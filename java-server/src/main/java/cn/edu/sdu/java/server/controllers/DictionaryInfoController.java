package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.DictionaryInfoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dictionary")
public class DictionaryInfoController {

    private final DictionaryInfoService dictionaryInfoService;

    public DictionaryInfoController(DictionaryInfoService dictionaryInfoService) {
        this.dictionaryInfoService = dictionaryInfoService;
    }

    @PostMapping("/findAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT') or hasRole('TEACHER')")
    public DataResponse findAll() {
        return dictionaryInfoService.findAll();
    }
}
