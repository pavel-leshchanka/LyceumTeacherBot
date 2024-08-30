package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DialogAttributeRepository extends JpaRepository<DialogAttribute, Long> {

    Optional<DialogAttribute> findByDialogId(Long dialogId);

    @Modifying
    @Transactional
    long deleteByDialogId(Long dialogId);
}
