package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;

import com.selimhorri.app.domain.Order;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    Counter counter;

    @InjectMocks
    OrderServiceImpl orderService;

    @BeforeEach
    void setup() {
        when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Test
    void findById_returnsDto() {
        com.selimhorri.app.domain.Cart cart = com.selimhorri.app.domain.Cart.builder()
            .cartId(99)
            .userId(1)
            .build();
        Order order = Order.builder().orderId(1).orderDesc("x").orderFee(10.0).cart(cart).build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        OrderDto dto = orderService.findById(1);
        assertEquals(1, dto.getOrderId());
        assertEquals("x", dto.getOrderDesc());
        assertEquals(10.0, dto.getOrderFee());
    }

    @Test
    void save_mapsAndPersists() {
        com.selimhorri.app.dto.CartDto cartDto = com.selimhorri.app.dto.CartDto.builder()
            .cartId(99)
            .userId(1)
            .build();
        OrderDto in = OrderDto.builder().orderDesc("x").orderFee(10.0).cartDto(cartDto).build();
        com.selimhorri.app.domain.Cart cart = com.selimhorri.app.domain.Cart.builder()
            .cartId(99)
            .userId(1)
            .build();
        Order persisted = Order.builder().orderId(2).orderDesc("x").orderFee(10.0).cart(cart).build();
        when(orderRepository.save(any(Order.class))).thenReturn(persisted);
        OrderDto out = orderService.save(in);
        assertEquals(2, out.getOrderId());
    }
}