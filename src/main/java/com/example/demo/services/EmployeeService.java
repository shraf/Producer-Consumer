package com.example.demo.services;

import com.example.demo.models.Employee;
import com.example.demo.repositories.EmployeeRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManagerFactory emf;

    public  void  mapBase64ToEmployees(Employee employee){
    }

    @Async
    public void convertImageToBase64(Employee employee){
        try {

            System.out.println("b");
        String uniqueID = UUID.randomUUID().toString();
          Path path = FileSystems.getDefault().getPath("").toAbsolutePath();
          String root = Paths.get(path.toString(), "src", "main", "resources", "imgs").toString();
            byte[] fileContent = FileUtils.readFileToByteArray(new File(root+"/"+employee.getImageurl()));
            String encodedString = Base64.getEncoder().encodeToString(fileContent);
            employee.setImageurl(encodedString);
        } catch (IOException e) {
            employee.setImageurl("");
            System.out.println(e.getMessage());
            return;
        }

    }

}
