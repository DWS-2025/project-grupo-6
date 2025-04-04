package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import es.xpressaly.Model.Product;
import es.xpressaly.Service.ProductService;

@Controller
public class ImageController {

    @Autowired
    private ProductService productService;
    
    @GetMapping("/image/{productId}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        
        if (product == null || product.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        
        return new ResponseEntity<>(product.getImageData(), headers, HttpStatus.OK);
    }
}