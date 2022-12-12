package com.example.springSecurityApplication.controllers.admin;

import com.example.springSecurityApplication.models.Image;
import com.example.springSecurityApplication.models.Order;
import com.example.springSecurityApplication.models.Product;
import com.example.springSecurityApplication.repositories.CategoryRepository;
import com.example.springSecurityApplication.security.PersonDetails;
import com.example.springSecurityApplication.services.PersonService;
import com.example.springSecurityApplication.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
//@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class AdminController {

    @Value("${upload.path}")
    private String uploadPath;

    private final ProductService productService;

    private final PersonService personService;

    private final CategoryRepository categoryRepository;


    @Autowired
    public AdminController(ProductService productService, CategoryRepository categoryRepository, PersonService personService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.personService = personService;
    }

    //    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @GetMapping("")
    public String admin(Model model){
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("person", personService.getAllPerson());
        return "admin/admin";
    }

    // http:8080/localhost/admin/product/add
    // Метод по отображению страницы с возможностью добавления товаров
    @GetMapping("/product/add")
    public String addProduct(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("category", categoryRepository.findAll());
        return "product/addProduct";
    }

    // Метод по добавлению продукта в БД через сервис->репозиторий
    @PostMapping("/product/add")
    public String addProduct(@ModelAttribute("product") @Valid Product product, BindingResult bindingResult, @RequestParam("file_one")MultipartFile file_one) throws IOException {
        if(bindingResult.hasErrors())
        {
            return "product/addProduct";
        }

        if(file_one != null)
        {
            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()){
                uploadDir.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + file_one.getOriginalFilename();
            file_one.transferTo(new File(uploadPath + "/" + resultFileName));
            Image image = new Image();
            image.setProduct(product);
            image.setFileName(resultFileName);
            product.addImageToProduct(image);
        }
        productService.saveProduct(product);
        return "redirect:/admin";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id){
        productService.deleteProduct(id);
        return "redirect:/admin";
    }

    // Метод по отображению страницы с возможностью редактирования товаров
    @GetMapping("/product/edit/{id}")
    public String editProduct(Model model, @PathVariable("id") int id){
        model.addAttribute("product", productService.getProductId(id));
        model.addAttribute("category", categoryRepository.findAll());
        return "product/editProduct";
    }

    // Метод по редактированию товара
    @PostMapping("/product/edit/{id}")
    public String editProduct(@ModelAttribute("product") @Valid Product product, BindingResult bindingResult, @PathVariable("id") int id){
        if(bindingResult.hasErrors())
        {
            return "product/editProduct";
        }
        productService.updateProduct(id, product);
        return "redirect:/admin";
    }


}

