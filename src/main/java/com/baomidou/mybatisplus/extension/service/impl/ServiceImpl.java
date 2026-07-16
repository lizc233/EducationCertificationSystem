package com.baomidou.mybatisplus.extension.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;

public class ServiceImpl<M extends BaseMapper<T>, T>
        extends com.baomidou.mybatisplus.spring.service.impl.ServiceImpl<M, T>
        implements IService<T> {
}
