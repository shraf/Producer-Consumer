package com.example.demo.controllers;

import com.example.demo.models.Employee;
import com.example.demo.repositories.EmployeeRepository;
import com.example.demo.services.EmployeeService;
import com.example.demo.services.FileStorageService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
@CrossOrigin(origins = "*")
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
    public CompletableFuture<List<Employee>> getAllEmployees(){
        List<Employee> employees = employeeRepository.findAll();
        List<CompletableFuture<Employee>> futures = new ArrayList<CompletableFuture<Employee>>() {
        };
        employees.stream().forEach(employee ->{
            futures.add(employeeService.convertImageToBase64(employee));
            System.out.println("thread a");
        });


        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));

    }

    @GetMapping("/search")
    public List<Employee> getAllEmployeesByQuery(@RequestParam(value="query",required = false) String query)
    {
        List<Employee> employees = employeeRepository.findByEmailContainsOrSsnContains(query, query);
        return employees;
    }

    @GetMapping("/csv")
    public void getCSV(@RequestParam("query")String query) {
        boolean isFirst = true;
        String[] querySplit = query.split(":");
        try  {
            BufferedReader reader = new BufferedReader(new FileReader(fileStorageService.getRootPath() + "/ofile.csv"), 10);
            fileStorageService.asyncSearchInCSV(querySplit,reader,"SIC CODE", "SIC DESCRIPTION", "COMPANY NAME", "CONTACT NAME", "title", "ADDRESS", "CITY", "STATE", "ZIP",
                    "PHONE", "FAX", "EMAIL", "WEB", "SITE");
            } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
        @PostMapping(value="/", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public void saveEmployee(@ModelAttribute Employee employee, @RequestParam(value = "image", required = false)MultipartFile fileImage, @RequestParam(value="image64", required = false) String base64String)
    {
        String imageUrl="";
        if(fileImage!=null)
            imageUrl = fileStorageService.save(fileImage);
        else{
            imageUrl = fileStorageService.save(base64String);
        }

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
