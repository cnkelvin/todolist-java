package br.com.kelvinnielson.todolist.task;

import br.com.kelvinnielson.todolist.utils.Utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

	@Autowired
	private ITaskRepository taskRepository;

	@PostMapping("/")
	public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
		var idUser = request.getAttribute("idUser");
		taskModel.setIdUser((UUID) idUser);

		// Se o startAt ou o endAt for uma data anterior ao currentDate, emitir um erro.
		var currentDate = LocalDateTime.now();
		if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("A data de início/data de término deve ser maior do que a data atual");
		}

		// Se o startAt for uma data após ao endAt, emitir um erro.
		if(taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("A data de início deve ser menor do que a data de término");
		}

		var task = this.taskRepository.save(taskModel);
		return ResponseEntity.status(HttpStatus.OK).body(task);
	}

	@GetMapping("/")
	public List<TaskModel> list(HttpServletRequest request) {
		var idUser = request.getAttribute("idUser");
		var tasks = this.taskRepository.findByIdUser((UUID) idUser);
		return tasks; 
	}

	// http://localhost:8080/tasks/505505505-ImGoingBackTo-505
	// Esse código vai poder fazer com que o usuário altere qualquer parte da tarefa dele;
	// Copiar os dados anteriores que não foram alterados;
	// Também vai fazer a validação do usuário, para ver se realmente é ele que está alterando a tarefa.
	@PutMapping("/{id}")
	public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
		var task = this.taskRepository.findById(id).orElse(null);

		if(task == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("Tarefa não encontrada");
		}

		var idUser = request.getAttribute("idUser");

		if(!task.getIdUser().equals(idUser)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body("Usuário não tem permissão para alterar essa tarefa");
		}

		Utils.copyNonNullProperties(taskModel, task);

		var taskUpdated = this.taskRepository.save(task);

		return ResponseEntity.ok().body(taskUpdated);
	}
}