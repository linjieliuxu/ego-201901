package com.ego.item.api;

import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/spec")
public interface SpecApi {

    @GetMapping("{id}")
    public ResponseEntity<String> querySpecificationByCategoryId(@PathVariable("id") Long id);
}
