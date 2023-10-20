package br.com.lucasfigueroa.todolist.task;


import br.com.lucasfigueroa.todolist.utils.Utils;
import jakarta.persistence.GeneratedValue;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){

        var idUser = request.getAttribute("idUser");
        taskModel.setUserId((UUID) idUser);

        var currentDate = LocalDateTime.now();

        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de inicio/termino deve ser maior que a data atual");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de inicio deve ser maior que a data termino");
        }


        TaskModel task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }


    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){
        var userId = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByUserId((UUID) userId);

        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){

        var userId = request.getAttribute("idUser");
        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("tarefa nao encontrada");
        }

        if (!task.getUserId().equals(userId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("usuario nao tem permissao");
        }

        Utils.copyNonNullProperties(taskModel,task);

        var taskUpdated = this.taskRepository.save(task);

        taskModel.setId(id);
        taskModel.setUserId((UUID) userId);

        this.taskRepository.save(task);

        return ResponseEntity.ok().body(this.taskRepository.save(taskUpdated));

    }


}
