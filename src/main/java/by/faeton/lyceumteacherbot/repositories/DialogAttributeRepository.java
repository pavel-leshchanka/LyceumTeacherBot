package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DialogAttributeRepository  extends JpaRepository<DialogAttribute, Long> {

}
