package com.ego.item.controller;

import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.Brand;
import com.ego.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    @Autowired
    private BrandService brandService;

    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> page(
            @RequestParam(value = "pageNo",defaultValue = "1")Integer page,
            @RequestParam(value = "pageSize",defaultValue = "5")Integer pageSize,
            @RequestParam(value = "descending",defaultValue = "true")Boolean descending,
            @RequestParam(value = "sortBy")String sortBy,
            @RequestParam(value = "key")String key
    )
    {
        PageResult<Brand> result = brandService.page(page, pageSize, descending, sortBy,key);
        if(result==null||result.getItems().size()==0)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Void> save(Brand brand, @RequestParam("cids") List<Long> cids) {
        brandService.save(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


//    http://api.ego.com/api/item/brand/cid/76
    @GetMapping("/cid/{cid}")
    public ResponseEntity<List<Brand>> queryListByCid(
            @PathVariable(value = "cid")Long cid
    )
    {
        List<Brand> result = brandService.queryListByCid(cid);
        if(result==null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/bid/{bid}")
    public ResponseEntity<Brand> queryBrandByBid(@PathVariable("bid") Long bid){
        Brand result = brandService.queryBrandByBid(bid);
        if(result==null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Brand>> queryListByIds(@RequestParam("ids")List<Long> ids){
        List<Brand> result = brandService.queryListByIds(ids);
        if(result==null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
