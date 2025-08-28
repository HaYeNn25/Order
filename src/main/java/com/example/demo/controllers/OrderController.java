package com.example.demo.controllers;
import com.example.demo.components.LocalizationUtils;
import com.example.demo.dtos.OrderDTO;
import com.example.demo.dtos.OrderDetailDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.Order;
import com.example.demo.models.OrderDetail;
import com.example.demo.responses.*;
import com.example.demo.services.IOrderService;
import com.example.demo.services.OrderDetailService;
import com.example.demo.untils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final LocalizationUtils localizationUtils;
    private final OrderDetailService orderDetailService;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ObjectResponse> insertOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMess = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.ok().body(ObjectResponse.builder()
                    .message(errorMess.toString())
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null)
                    .build());
        }
        Order order = orderService.createOrder(orderDTO);
        return ResponseEntity.ok().body(ObjectResponse.builder()
                .message("Insert order successfully")
                .status(HttpStatus.CREATED)
                .data(order)
                .build());
    }

    @GetMapping("/user/{user_id}")
    public  ResponseEntity<ObjectResponse> getOrders(@Valid @PathVariable("user_id") Long userId) throws Exception{
        List<Order> orders = orderService.findByUserId(userId);
        return ResponseEntity.ok(
                new ObjectResponse(
                        "Get list of orders successfully",
                        HttpStatus.OK,
                        orders));
    }

    @GetMapping("/{id}")
    public  ResponseEntity<ObjectResponse> getOrder(@Valid @PathVariable("id") Long orderId) throws Exception{
        Order existingOrder = orderService.getOrder(orderId);OrderResponse.fromOrder(existingOrder);
        return ResponseEntity.ok(
                new ObjectResponse("Get order successfully",HttpStatus.OK,existingOrder));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ObjectResponse> updateOrder(
            @Valid @PathVariable Long id,
            @Valid @RequestBody OrderDTO orderDTO
    ) throws Exception {
        Order order = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(new ObjectResponse("Update order successfully", HttpStatus.OK, order));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ObjectResponse> deleteOrder(@Valid @PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(
                ObjectResponse.builder()
                        .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY, id))
                        .build());
    }

    @GetMapping("/get-orders-by-keyword")
    public ResponseEntity<ObjectResponse> getOrdersByKeyword(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by("id").ascending()
        );
        Page<OrderResponse> orderResponsePage = orderService
                                                .getOrdersByKeyword(keyword, pageRequest)
                                                .map(OrderResponse::fromOrder);
        OrderListResponse response = OrderListResponse.builder()
                .orders(orderResponsePage.getContent())
                .totalPages(orderResponsePage.getTotalPages())
                .currentPage(page)
                .build();
        List<OrderResponse> orderResponseList = orderResponsePage.getContent();
        return ResponseEntity.ok().body(ObjectResponse.builder()
                .message("Get orders successfully")
                .status(HttpStatus.OK)
                .data(response)
                .build());
    }

    @PostMapping("/order_details")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ObjectResponse> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO) throws Exception {
        OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(newOrderDetail);
        return ResponseEntity.ok().body(
                ObjectResponse.builder()
                        .message("Create order detail successfully")
                        .status(HttpStatus.CREATED)
                        .data(orderDetailResponse)
                        .build()
        );
    }

    @GetMapping("/order_details/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id
    ) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
        return ResponseEntity.ok().body(
                ObjectResponse.builder()
                        .message("Get order detail successfully")
                        .status(HttpStatus.OK)
                        .data(OrderDetailResponse.fromOrderDetail(orderDetail))
                        .build()
        );
    }

    //lấy ra danh sách các order_details của 1 order nào đó
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @Valid @PathVariable("orderId") Long orderId
    ) {
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = orderDetails
                .stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .toList();
        return ResponseEntity.ok().body(
                ObjectResponse.builder()
                        .message("Get order details by orderId successfully")
                        .status(HttpStatus.OK)
                        .data(orderDetailResponses)
                        .build()
        );
    }

    @PutMapping("/order_details/{id}")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public  ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO orderDetailDTO
    ) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
        return ResponseEntity.ok().body(ObjectResponse
                .builder()
                .data(OrderDetailResponse.fromOrderDetail(orderDetail))
                .message("Update order detail successfully")
                .status(HttpStatus.OK)
                .build());
    }

    @DeleteMapping("/order_details/{id}")
    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteOrderDetail(
            @Valid @PathVariable("id") Long id
    ) {
        orderDetailService.deleteById(id);
        return ResponseEntity.ok(
                ObjectResponse.builder()
                .message(localizationUtils
                        .getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY, id))
                .build());
    }
}
