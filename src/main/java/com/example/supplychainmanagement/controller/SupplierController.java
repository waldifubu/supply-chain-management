package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.entity.Product;
import com.example.supplychainmanagement.entity.RequestComponent;
import com.example.supplychainmanagement.entity.Storage;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.enterprise.RequestPartsOrderResponse;
import com.example.supplychainmanagement.model.enums.RequestStatus;
import com.example.supplychainmanagement.model.enums.StorageStatus;
import com.example.supplychainmanagement.repository.RequestComponentRepository;
import com.example.supplychainmanagement.repository.StorageRepository;
import com.example.supplychainmanagement.repository.UserRepository;
import com.example.supplychainmanagement.service.business.AdminService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supplier")
public class SupplierController {

    private final RequestComponentRepository requestComponentRepository;

    private final UserRepository userRepository;

    private final StorageRepository storageRepository;

    private final AdminService adminService;

    /**
     * Get all open requests, or if wished, parameterized
     *
     * @param requiredStatus
     * @param authUser
     * @return
     */
    @GetMapping(value = "/requests")
    @Secured({"ROLE_SUPPLIER", "ROLE_ADMIN"})
    public ResponseEntity<?> getAllRequestsByStatus(
            @RequestParam(value = "status", defaultValue = "open") String requiredStatus,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {

        RequestStatus requestStatus;
        List<RequestComponent> requestComponentList;

        String requestStatusString = requiredStatus.toUpperCase();
        requestStatus = RequestStatus.valueOf(requestStatusString);

        //admin check order open
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();
        var isAdmin = adminService.isAdmin(user);

        if (isAdmin || requestStatus == RequestStatus.OPEN) {
            requestComponentList = requestComponentRepository.findAllByRequestStatusOrderByRequestDate(requestStatus);
        } else {
            requestComponentList = requestComponentRepository.findAllByRequestStatusAndSupplier(requestStatus, user);
        }

        List<RequestPartsOrderResponse> responseList = getRequestPartsOrderResponses(requestComponentList);
        try {
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Just for a good visualization
     *
     * @param requestComponentList
     * @return
     */
    private static List<RequestPartsOrderResponse> getRequestPartsOrderResponses(List<RequestComponent> requestComponentList) {
        List<RequestPartsOrderResponse> responseList = new ArrayList<>();

        for (RequestComponent requestComponent : requestComponentList) {
            var response = new RequestPartsOrderResponse(
                    requestComponent.getId(),
                    requestComponent.getComponent().getArticleNo(),
                    requestComponent.getQty(),
                    requestComponent.getComment(),
                    requestComponent.getRequestStatus(),
                    requestComponent.getRequestDate()
            );

            responseList.add(response);
        }

        return responseList;
    }

    /**
     * We will approve a certain RequestComponent - We handle the request, ID is a UUID
     * Note: Only Supplier role is allowed to approve the request
     *
     * @param id
     * @param authUser
     * @return
     */
    @PatchMapping(value = "/approve/{id}")
    @Secured({"ROLE_SUPPLIER"})
    public ResponseEntity<?> approveRequest(@PathVariable("id") final UUID id,
                                            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {
        try {
            RequestComponent requestComponent = processRequest(id, authUser, RequestStatus.APPROVED);

            return new ResponseEntity<>(requestComponent, HttpStatus.OK);
        } catch (AccessDeniedException ace) {
            return handleWrongDate(ace.getMessage());
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Now twe send the requested component/s and make a message to our storage
     *
     * @param id
     * @param authUser
     * @return
     */
    @PatchMapping(value = "/send/{id}")
    @Secured({"ROLE_SUPPLIER", "ROLE_ADMIN"})
    public ResponseEntity<?> transitRequestComponent(@PathVariable("id") final UUID id,
                                                     @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {
        try {
            RequestComponent requestComponent = processRequest(id, authUser, RequestStatus.IN_TRANSIT);
            Product p = requestComponent.getComponent().getProduct();

            Optional<Storage> optionalStorage = storageRepository.findByProductFullStorage(p);
            Storage storage = optionalStorage.orElseThrow();

            storage.setStorageStatus(StorageStatus.IN_DELIVERY);
            storageRepository.save(storage);

            return new ResponseEntity<>(requestComponent, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * The requested parts are delivered - We are done
     *
     * @param id
     * @param authUser
     * @return
     */
    @PatchMapping(value = "/delivered/{id}")
    @Secured({"ROLE_SUPPLIER", "ROLE_ADMIN"})
    public ResponseEntity<?> deliverRequestComponent(@PathVariable("id") final UUID id,
                                                     @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {
        try {
            RequestComponent requestComponent = processRequest(id, authUser, RequestStatus.STORAGE);

            return new ResponseEntity<>(requestComponent, HttpStatus.OK);
        } catch (AccessDeniedException ace) {
            return handleWrongDate(ace.getMessage());
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Process whole workflow
     *
     * @param id
     * @param authUser
     * @param requestStatus
     * @return
     */
    private RequestComponent processRequest(UUID id,
                                            org.springframework.security.core.userdetails.User authUser,
                                            RequestStatus requestStatus) {

        Optional<RequestComponent> optionalRequestParts = requestComponentRepository.getById(id);
        RequestComponent requestComponent = optionalRequestParts.orElseThrow();
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();

        var isAdmin = adminService.isAdmin(user);
        // First check if we are not Admin request is allowed to take
        if (requestComponent.getSupplier() != null && !requestComponent.getSupplier().getEmail().equals(user.getEmail())
                && requestComponent.getRequestStatus() != RequestStatus.OPEN) {
            if(!isAdmin) {
                throw new AccessDeniedException("Request is taken by another account");
            }
        }

        // We are not allowed to repeat status and, of course, we can't go back
        if (requestStatus.ordinal() <= requestComponent.getRequestStatus().ordinal()) {
            throw new AccessDeniedException("Wrong status: " + requestComponent.getRequestStatus());
        }

        if (!isAdmin) {
            requestComponent.setSupplier(user);
        }
        requestComponent.setRequestStatus(requestStatus);
        requestComponent.setUpdated(LocalDateTime.now());
        requestComponentRepository.save(requestComponent);
        requestComponent.setArticleCode(requestComponent.getComponent().getArticleNo());

        return requestComponent;
    }

    private ResponseEntity<JsonNode> handleWrongDate(String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("error", HttpStatus.UNAUTHORIZED.value());
        objectNode.put("message", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(objectNode);
    }
}
