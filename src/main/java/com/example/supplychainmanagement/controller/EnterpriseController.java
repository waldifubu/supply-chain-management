package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.dto.CompactOrder;
import com.example.supplychainmanagement.entity.*;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.customer.ListOrders;
import com.example.supplychainmanagement.model.enterprise.ListProducts;
import com.example.supplychainmanagement.model.enterprise.RequestPartsOrderRequest;
import com.example.supplychainmanagement.model.enterprise.RequestPartsOrderResponse;
import com.example.supplychainmanagement.model.enums.*;
import com.example.supplychainmanagement.repository.*;
import com.example.supplychainmanagement.service.business.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrdersProductsRepository ordersProductsRepository;
    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final RoleRepository roleRepository;
    private final RequestComponentRepository requestComponentRepository;
    private final ComponentRepository componentRepository;
    private final OrderService orderService;

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseBody
    public ResponseEntity<JsonNode> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("error", HttpStatus.BAD_REQUEST.value());
        objectNode.put("message", name + " parameter is missing");
        return ResponseEntity.badRequest().body(objectNode);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    @ResponseBody
    public ResponseEntity<Object> authMissing(org.springframework.web.bind.MissingRequestHeaderException ex) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("error", HttpStatus.BAD_REQUEST.value());
        objectNode.put("message", "Auth problem");
        return ResponseEntity.badRequest().body(objectNode);
    }

    private ResponseEntity<Object> handleWrongData(String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("error", HttpStatus.BAD_REQUEST.value());
        objectNode.put("message", message);
        return ResponseEntity.badRequest().body(objectNode);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseBody
    public ResponseEntity<JsonNode> handleUnknownParams(IllegalArgumentException ex) {
        String name = ex.getMessage();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("error", HttpStatus.BAD_REQUEST.value());
        objectNode.put("message", name + " unknown status");
        return ResponseEntity.badRequest().body(objectNode);
    }

    @GetMapping("/orders")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> getAllOrders(
            @RequestParam(value = "status") Optional<String> requiredStatus,
            @RequestParam(value = "date", required = false) Optional<String> optionalDate
    ) throws MissingServletRequestParameterException, IllegalArgumentException {
        if (requiredStatus.isEmpty()) {
            throw new MissingServletRequestParameterException("status", "error");
        }

        String requestStatus = requiredStatus.get().toUpperCase();
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(requestStatus);
        } catch (IllegalArgumentException | NullPointerException iae) {
            return handleWrongData("Data is wrong: " + requestStatus);
        }

        Date selectedDate;
        List<Order> orderList;

        if (optionalDate.isPresent()) {
            try {
                selectedDate = Date.valueOf(optionalDate.get());
                orderList = orderRepository.findOrderByStatusAndOrderDate(orderStatus, selectedDate);
            } catch (IllegalArgumentException iae) {
                return handleWrongData("Date is wrong: " + optionalDate.get());
            }
        } else {
            orderList = orderRepository.findOrderByStatus(orderStatus);
        }

        //Important: Here, we change status from every created order to open
        //It means somebody reviewed the order
        orderRepository.saveAll(
                orderList.stream()
                        .filter(order -> order.getStatus() == OrderStatus.CREATED)
                        .peek(o -> o.setStatus(OrderStatus.OPEN))
                        .toList()
        );

        orderList = orderList.stream()
                .filter(order -> order.getOrdersProducts() != null)
                .peek(order -> order.setCountProducts(order.getOrdersProducts().size()))
                .peek(order -> order.setOrdersProducts(null))
                .toList();

        try {
            ListOrders listOrders = new ListOrders(orderList);
            return new ResponseEntity<>(listOrders, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/order/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<CompactOrder> getOrderDetails(
            @PathVariable("id") long id) {

        Optional<Order> orderData = orderRepository.findOrderByOrderNo(id);
        Order order = orderData.orElseThrow();

        //It means somebody reviewed the order
        if (order.getStatus() == OrderStatus.CREATED) {
            order.setStatus(OrderStatus.OPEN);
            orderRepository.save(order);
        }

        CompactOrder compactOrder = orderService.createCompactOrder(order);

        try {
            return new ResponseEntity<>(compactOrder, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/storage/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<Object> getStorageDetails(
            @PathVariable("id") long articleNo,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {

        Product product = productRepository.findProductByArticleNo(articleNo);
        if (null == product) {
            return handleWrongData("Product not found");
        }

        Optional<Storage> optionalStorage;

        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();
        Optional<Role> optionalRole = roleRepository.findByName(UserRole.ROLE_ADMIN.toString());
        boolean isAdmin = false;
        // First check, if we are admin
        if (optionalRole.isPresent() && user.getRoles().contains(optionalRole.get())) {
            optionalStorage = storageRepository.findByProductFullStorage(product);
            List<Component> componentList = componentRepository.findAllByProduct(product);
            product.setComponentList(componentList);
            isAdmin = true;
        } else {
            optionalStorage = storageRepository.findByProduct(product);
        }
        Storage storage = optionalStorage.orElseGet(Storage::new);
        storage.setProductName(product.getName());

        if(isAdmin){
            storage.setProduct(product);
        }

        try {
            return new ResponseEntity<>(storage, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/calculate/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> calculateStock(@PathVariable("id") long orderNo) {
        Optional<Order> orderData = orderRepository.findOrderByOrderNo(orderNo);
        Order order = orderData.orElseThrow();

        List<OrdersProducts> opList = ordersProductsRepository.findOrdersProductsByOrderNo(order);
        List<Product> missingList = new ArrayList<>();

        for (OrdersProducts op : opList) {
            Optional<Storage> optionalStorage = storageRepository.findByProduct(op.getProduct());
            Storage storage = optionalStorage.orElseThrow();
            Product p = op.getProduct();
            p.setQty(op.getQty());
            p.setInStock(storage.getStock());

            //Calculate which products are needed
            if (storage.getStock() >= op.getQty()) {
                op.setStatus(ProductStatus.IN_STOCK);
            } else {
                op.setStatus(ProductStatus.OUT_OF_STOCK);
                missingList.add(p);
            }

            ordersProductsRepository.save(op);
            if (order.getStatus() == OrderStatus.OPEN) {
                order.setStatus(OrderStatus.REVIEW);
                orderRepository.save(order);
            }
        }

        return new ResponseEntity<>(missingList, HttpStatus.OK);
    }

    @PostMapping("/request/product")
    public ResponseEntity<?> requestProductWithAllParts(
            @RequestBody RequestPartsOrderRequest productRequest
    ) {
        Product product = productRepository.findProductByArticleNo(productRequest.getArticleNo());
        product.setComponentList(componentRepository.findAllByProduct(product));
        RequestComponent requestComponent;
        List<RequestComponent> requestComponentList = new ArrayList<>();

        for (int i = 0; i < productRequest.getQty(); i++) {
            for (Component component : product.getComponentList()) {
                requestComponent = requestOnePart(component);

                if (productRequest.getComment() != null) {
                    requestComponent.setComment(productRequest.getComment());
                }
                requestComponentRepository.save(requestComponent);
                requestComponentList.add(requestComponent);
            }
        }

        try {
            return new ResponseEntity<>(requestComponentList, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private RequestComponent requestOnePart(Component component) {
        RequestComponent requestComponent = new RequestComponent();
        requestComponent.setRequestDate(LocalDateTime.now());
        requestComponent.setQty(1);
        requestComponent.setComponent(component);
        requestComponent.setArticleCode(component.getArticleNo());

        Product product = requestComponent.getComponent().getProduct();

        Optional<Storage> optionalStorage = storageRepository.findByProductFullStorage(product);
        Storage storage = optionalStorage.orElseThrow();
        storage.setStorageStatus(StorageStatus.REQUESTED);
        storageRepository.save(storage);

        return requestComponent;
    }

    // @TODO: Maybe resolve with loop?
    private RequestComponent requestComponent(Component component, int qty) {
        var requestComponent = requestOnePart(component);
        requestComponent.setQty(qty);

        return requestComponent;
    }

    @PostMapping("/request/part")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> requestParts(
            @RequestBody RequestPartsOrderRequest partsRequest
    ) {
        Component component = componentRepository.getPartByArticleNo(partsRequest.getArticleCode());

        RequestComponent reqComponent = requestComponent(component, partsRequest.getQty());
        if (partsRequest.getComment() != null) {
            reqComponent.setComment(partsRequest.getComment());
        }
        requestComponentRepository.save(reqComponent);

        var response = new RequestPartsOrderResponse(
                reqComponent.getId(),
                component.getArticleNo(),
                reqComponent.getQty(),
                reqComponent.getComment(),
                reqComponent.getRequestStatus(),
                reqComponent.getRequestDate()
        );

        try {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/product/{articleNo}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<Product> productInfo(@PathVariable("articleNo") long articleNo) {
        Product product = productRepository.findProductByArticleNo(articleNo);
        product.setComponentList(componentRepository.findAllByProduct(product));
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    // @TODO: Rethink delivery date
    @GetMapping("/storage/check")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<Object> checkInventory() {
        // 1st Give me all request_components which are delivered
        List<RequestComponent> requestComponentList = requestComponentRepository.findAllByRequestStatusOrderByRequestDate(RequestStatus.STORAGE);
        // 2nd return all products who can build with ONE of these parts. Set is used to avoid duplicate products
        Set<Product> possibleProductsSet = requestComponentList.stream().map(request -> request.getComponent().getProduct()).collect(Collectors.toSet());

        List<Product> productList = new ArrayList<>();
        Product completeProduct;

        try {
            for (Product product : possibleProductsSet) {
                // We try to build a product with given parts list
                completeProduct = assemblyComponentToProduct(product, requestComponentList);

                // if !null means, a complete product was produced
                if (null != completeProduct) {
                    productList.add(completeProduct);
                    Optional<Storage> optionalStorage = storageRepository.findByProduct(completeProduct);
                    Storage storage = optionalStorage.orElseGet(Storage::new);
                    storage.setStock(storage.getStock()+1);
                    storage.setUpdated(LocalDateTime.now());
//                    storage.setLastDelivery(new Date(java.lang.System.currentTimeMillis()));
                    // @TODO: If we an empty storage
                    //storage.setProduct(completeProduct);
                    storageRepository.save(storage);
                }
            }

            // Return a list of all produced products. Could also be empty
            ListProducts listProducts = new ListProducts(productList, productList.size());

            return new ResponseEntity<>(listProducts, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private Product assemblyComponentToProduct(Product product, List<RequestComponent> requestComponentList) {
        Map<UUID, RequestComponent> requestComponentMap = new HashMap<>();
        product.setComponentList(componentRepository.findAllByProduct(product));
        int listSize = product.getComponentList().size();

        for (Component component : product.getComponentList()) {
            for (RequestComponent requestComponent : requestComponentList) {
                if (Objects.equals(component.getId(), requestComponent.getComponent().getId())) {
                    requestComponentMap.put(requestComponent.getId(), requestComponent);
                }
            }
        }

        if (requestComponentMap.size() == listSize) {
            RequestComponent requestComponent = null;
            for (UUID uuid : requestComponentMap.keySet()) {
                Optional<RequestComponent> optionalRequestComponent = requestComponentRepository.getById(uuid);
                requestComponent = optionalRequestComponent.orElseThrow();

                requestComponent.setQty(requestComponent.getQty() - 1);
                requestComponent.setUpdated(LocalDateTime.now());

                if (requestComponent.getQty() == 0) {
                    requestComponent.setRequestStatus(RequestStatus.ASSEMBLED);
                }

                requestComponentRepository.save(requestComponent);
            }

            if (null != requestComponent) {
                Optional<Storage> optionalStorage = storageRepository.findByProduct(requestComponent.getComponent().getProduct());
                Storage storage = optionalStorage.orElseThrow();
//                storage.setLastDelivery(new Date(Calendar.getInstance().getTime().getTime()));
                storageRepository.save(storage);

                return requestComponent.getComponent().getProduct();
            }
        }

        return null;
    }

    @GetMapping("/autorequest/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> automaticRequest(
            @PathVariable("id") long id
    ) {
        try {
            Optional<Order> orderData = orderRepository.findOrderByOrderNo(id);
            Order order = orderData.orElseThrow();
            RequestComponent requestComponent;

            List<OrdersProducts> opList = order.getOrdersProducts();
            List<RequestComponent> requestComponentList = new ArrayList<>();

            for (OrdersProducts op : opList) {
                OrdersProducts ordersProducts = ordersProductsRepository.getReferenceById(op.getId());
                ordersProducts.setStatus(ProductStatus.PENDING);
                ordersProductsRepository.save(ordersProducts);

                Product p = op.getProduct();
                p.setComponentList(componentRepository.findAllByProduct(p));

                for (int i = 0; i < op.getQty(); i++) {
                    for (Component component : p.getComponentList()) {
                        requestComponent = requestOnePart(component);
                        requestComponentRepository.save(requestComponent);
                        requestComponentList.add(requestComponent);
                    }
                }
            }

            return new ResponseEntity<>(requestComponentList, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/proceed/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> proceed(
            @PathVariable("id") long id
    ) {
        try {
            Optional<Order> orderData = orderRepository.findOrderByOrderNo(id);
            Order order = orderData.orElseThrow();

            if (order.getStatus().ordinal() > 2) {
                return handleWrongData("Order is in wrong status. Current Status: " + StringUtils.capitalize(order.getStatus().toString().toLowerCase()));
            }

            List<OrdersProducts> opList = order.getOrdersProducts();
            List<OrdersProducts> updatedList = new ArrayList<>();
            List<OrdersProducts> commissionList = new ArrayList<>();

            boolean allInStock = true;
            for (OrdersProducts op : opList) {
                Product product = op.getProduct();
                int amount = op.getQty();

                Optional<Storage> optionalStorage = storageRepository.findByProduct(product);
                Storage storage = optionalStorage.orElseThrow();

                if (storage.getStock() >= amount) {
                    op.getProduct().setProductStatus(ProductStatus.IN_STOCK);
                    op.getProduct().setInStock(op.getQty());
                    op.setStatus(ProductStatus.IN_STOCK);
                    updatedList.add(op);
                    commissionList.add(op);
                } else {
                    allInStock = false;
                }
            }

            if (allInStock) {
                ordersProductsRepository.saveAll(
                        commissionList.stream()
                                .filter(ordersProducts -> ordersProducts.getStatus() == ProductStatus.IN_STOCK)
                                .peek(o -> o.setStatus(ProductStatus.AVAILABLE))
                                .toList()
                );

                order.setOrdersProducts(commissionList);
                order.setStatus(OrderStatus.APPROVED);
            } else {
                order.setOrdersProducts(updatedList);
            }

            order.setUpdated(LocalDateTime.now());
            orderRepository.save(order);

            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/reject/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> rejectOrder(
            @PathVariable("id") long id
    ) {
        try {
            Optional<Order> orderData = orderRepository.findOrderByOrderNo(id);
            Order order = orderData.orElseThrow();

            if (order.getStatus() == OrderStatus.REVIEW) {
                order.setStatus(OrderStatus.REJECTED);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
            } else {
                return handleWrongData("Reject order " + id + " is not possible anymore");
            }

            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/picking/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> pickingOrder(
            @PathVariable("id") long id
    ) {
        try {
            Optional<Order> orderData = orderRepository.findOrderByOrderNo(id);
            Order order = orderData.orElseThrow();

            if (order.getStatus() == OrderStatus.APPROVED) {
                order.setStatus(OrderStatus.IN_COMMISSION);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
            } else {
                return handleWrongData("This status can't be handled anymore. It's already " + order.getStatus());
            }

            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/transfer/{id}")
    @Secured({"ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> readyForTransferOrder(
            @PathVariable("id") long id
    ) {
        try {
            Optional<Order> orderData = orderRepository.findOrderByOrderNo(id);
            Order order = orderData.orElseThrow();

            if (order.getStatus() == OrderStatus.IN_COMMISSION) {
                order.setStatus(OrderStatus.CONVEYABLE);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
            } else {
                return handleWrongData("Commissioned order No. " + id + " can't be changed");
            }

            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}