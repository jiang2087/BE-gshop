package com.example.demo.controllers.products;

import com.example.demo.dto.request.LaptopRequest;
import com.example.demo.services.products.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products/laptop")
public class LaptopController {

    private final LaptopService laptopService;

    @GetMapping
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok(laptopService.getAllLaptop());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getLaptopVariants(@PathVariable Long id){
        return ResponseEntity.ok(laptopService.getLaptopVariants(id));
    }

    @PutMapping
    public ResponseEntity<?> updateLaptop(@RequestParam String sku,@RequestBody LaptopRequest laptopRequest){
        return ResponseEntity.ok(laptopService.updateLaptop(sku, laptopRequest));
    }
    @PostMapping
    public ResponseEntity<?> addLaptop(@RequestBody LaptopRequest laptopRequest){
        return ResponseEntity.ok(laptopService.createLaptop(laptopRequest));
    }
    @DeleteMapping
    public ResponseEntity<?> deleteLaptop(@RequestParam String sku){
        laptopService.deleteLaptop(sku);
        return ResponseEntity.ok("Xóa thành công!");
    }
}
