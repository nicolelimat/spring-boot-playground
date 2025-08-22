package com.devsuperior.userdept.controllers;

import com.devsuperior.userdept.entities.Department;
import com.devsuperior.userdept.entities.User;
import com.devsuperior.userdept.repositories.DepartmentRepository;
import com.devsuperior.userdept.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    @Autowired // para não precisar instanciar
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    public List<User> findAll(){
        List<User> result = userRepository.findAll();
        return result;
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        Optional<User> user = userRepository.findById(id);

        if(user.isPresent()) {
            return ResponseEntity.ok(user.get()); // 200
        } else {
            return ResponseEntity.notFound().build(); // 404
        }

    }

    @PostMapping
    public User insert(@RequestBody User user){
        User result = userRepository.save(user);
        return result;
    }

    // response entity pra que não retorne status 200 sempre
    // todo: - retornar um objeto json com uma mensagem
    @DeleteMapping(value="/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        ResponseEntity<User> response = getUserById(id);
        if(response.getStatusCode().is2xxSuccessful()){
            userRepository.deleteById(response.getBody().getId());
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    @PutMapping(value = "/{id}") // atualiza o objeto user inteiro
    public ResponseEntity<User> updateUserById(@PathVariable Long id, @RequestBody User newUser){

        return userRepository.findById(id)
                // para cada user encontrado, seta as novas infos
                // por ser um Optional, se não encontrar ele retorna vazio, que é pegado
                // depois pelo orElseGet(() -> {...})
                .map(existingUser -> {
                    existingUser.setName(newUser.getName());
                    existingUser.setEmail(newUser.getEmail());
                    existingUser.setDepartment(newUser.getDepartment());

                    User updated = userRepository.save(existingUser);
                    return ResponseEntity.ok(updated); // 200 + user updated
                }).orElseGet(() -> ResponseEntity.notFound().build());

    }

    // Map<String, Object> deixa dinâmico os fields que vão ser atualizados, os dividindo em (chave, valor)
    @PatchMapping(value = "{id}")
    public ResponseEntity<User> updateUserFieldsById(@PathVariable Long id, @RequestBody Map<String, Object> fields){
        // optional: pode conter, ou não, um objeto. pode retornar vazio (trata null)
        Optional<User> userOPT = userRepository.findById(id);

        if(userOPT.isPresent()){
            User user = userOPT.get();
            fields.forEach((field, value) -> {
                // dinamicamente atualiza os fields passados
                switch (field) {
                    case "email":
                        user.setEmail((String) value);
                        break;
                    case "name":
                        user.setName((String) value);
                        break;
                    case "department":
                        // o value de department é outro map {"id": value}
                        Map<String, Object> deptMap = (Map<String, Object>) value;
                        Long deptId = Long.valueOf(deptMap.get("id").toString());

                        Optional<Department> deptOPT = departmentRepository.findById(deptId);
                        if (deptOPT.isEmpty()) {
                            // não tem tratamento para caso o deptOPT não exista
                        } else {
                            user.setDepartment(deptOPT.get());
                        }
                        break;
                }
            });

            userRepository.save(user);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }

    }
}
