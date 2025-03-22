package com.aippt.mapper;

import com.aippt.entity.Payment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Payment data access layer
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
} 