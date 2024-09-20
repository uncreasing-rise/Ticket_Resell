package com.swd392.ticket_resell_be.controllers;

import com.swd392.ticket_resell_be.dtos.requests.PackageDtoRequest;
import com.swd392.ticket_resell_be.dtos.requests.PackagePurchaseRequest;
import com.swd392.ticket_resell_be.dtos.responses.ApiItemResponse;
import com.swd392.ticket_resell_be.entities.Package;
import com.swd392.ticket_resell_be.entities.User;
import com.swd392.ticket_resell_be.exceptions.AppException;
import com.swd392.ticket_resell_be.repositories.TransactionRepository;
import com.swd392.ticket_resell_be.repositories.UserRepository;
import com.swd392.ticket_resell_be.services.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/packages")
public class PackageController {
    PackageService packageService;
    UserRepository userRepository;
    TransactionRepository transactionRepository;
    PayOS payOS;

    @PostMapping
    public ResponseEntity<ApiItemResponse<Package>> createPackage(@RequestBody @Valid PackageDtoRequest packageDtoRequest) {
        ApiItemResponse<Package> response = packageService.createPackage(packageDtoRequest);
        return ResponseEntity.ok(response); // Return OK for the creation response
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiItemResponse<Package>> getPackageById(@PathVariable("id") UUID packageId) {
        ApiItemResponse<Package> response = packageService.getPackageById(packageId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiItemResponse<List<Package>>> getAllPackages() {
        ApiItemResponse<List<Package>> response = packageService.getAllPackages();
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiItemResponse<Package>> updatePackage(
            @PathVariable("id") UUID packageId,
            @RequestBody @Valid PackageDtoRequest packageDtoRequest) {

        try {
            ApiItemResponse<Package> response = packageService.updatePackage(packageId, packageDtoRequest);
            return ResponseEntity.ok(response);
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiItemResponse<>(null, HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiItemResponse<>(null, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiItemResponse<Void>> deletePackage(@PathVariable("id") UUID packageId) {
        ApiItemResponse<Void> response = packageService.deletePackage(packageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchase")
    public ResponseEntity<String> purchasePackage(@RequestBody PackagePurchaseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Hoặc bất kỳ định danh nào khác của người dùng

        // Lấy thông tin người dùng từ username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        try {
            // Thực hiện quá trình mua gói và lấy checkoutUrl
            String checkoutUrl = packageService.purchasePackage(request.getPackageId(), user.getId());
            // Trả về checkoutUrl
            return ResponseEntity.status(HttpStatus.CREATED).body(checkoutUrl);
        } catch (AppException appEx) {
            // Xử lý các lỗi ứng dụng cụ thể
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(appEx.getMessage());
        } catch (Exception e) {
            // Xử lý lỗi chung
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/webhook/confirm-payment")
    public ResponseEntity<Void> confirmPayment(@RequestBody Webhook webhookBody) {
        try {
            WebhookData webhookData = payOS.verifyPaymentWebhookData(webhookBody);
            if (webhookBody.getSuccess()) {
                long orderCode = webhookData.getOrderCode();
                packageService.confirmPayment(orderCode);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
