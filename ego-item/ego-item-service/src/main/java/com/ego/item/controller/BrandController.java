package com.ego.item.controller;

import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/5/29
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

   // http://api.ego.com/api/item/brand/page?pageNo=1&pageSize=5&descending=false&sortBy=name&totalItems=0
    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> page(
            @RequestParam(value = "pageNo",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "5")Integer pageSize,
            @RequestParam(value = "descending",defaultValue = "true")Boolean descending,
            @RequestParam(value = "sortBy")String sortBy
    )
    {
//        PageResult<Brand> result = brandService.page(page, pageSize, descending, sortBy);
//        if(result==null||result.getItems().size()==0)
//        {
//            return ResponseEntity.notFound().build();
//        }
        return ResponseEntity.ok(null);
    }
}
