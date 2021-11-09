package com.example.demo.controllers;

import com.example.demo.models.Employee;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.services.EmployeeService;
import com.example.demo.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private EmployeeService employeeService;
    @GetMapping("/")
    public List<Employee> getAllEmployees(){
        List<Employee> employees = employeeRepository.findAll();
        employees.stream().forEach(employee ->{ employeeService.convertImageToBase64(employee);System.out.println("a");});
        return employees;
    }

    @GetMapping("/search")
    public List<Employee> getAllEmployeesByQuery(@RequestParam(value="query",required = false) String query)
    {
        List<Employee> employees = employeeRepository.findByEmailContainsOrSsnContains(query, query);
        return employees;
    }

    @PostMapping(value="/", consumes = "multipart/form-data")
    public void saveEmployee(@ModelAttribute Employee employee, @RequestParam("image")MultipartFile file){
        String imageUrl = fileStorageService.save(file);
        employee.setImageurl(imageUrl);
        Employee newEmployee = employeeRepository.save(employee);
    }

    @PutMapping(value="/{id}",consumes = "multipart/form-data")
    public ResponseEntity<Employee> editEmployee(@ModelAttribute Employee employee, @RequestParam(value = "image", required = false) MultipartFile file,@PathVariable("id") long id){
        Employee oldEmployee = employeeRepository.findById(id).orElse(null);
        String imageUrl;
        if(file!=null){
            imageUrl = fileStorageService.save(file);
            oldEmployee.setImageurl(imageUrl);
        }
        if(oldEmployee == null)
            return (ResponseEntity<Employee>) ResponseEntity.notFound();
        System.out.println(employee.getEmail());
        oldEmployee.setEmail(employee.getEmail());
        oldEmployee.setSsn(employee.getSsn());
        employeeRepository.save(oldEmployee);
        return ResponseEntity.ok().body(oldEmployee);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable long id){
        employeeRepository.deleteById(id);
    }

}
