package br.com.jmcodestudio.megabarros.application.usecase.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceRepositoryPort;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.DeleteApoliceUseCase;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApoliceDeleteUseCaseImpl implements DeleteApoliceUseCase {

    private final ApoliceRepositoryPort repo;
    private final CurrentUserPort currentUser;

    public ApoliceDeleteUseCaseImpl(ApoliceRepositoryPort repo, CurrentUserPort currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    @Override
    public void deleteById(ApoliceId id) {
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem excluir apólices.");
        }
        repo.deleteById(id);
    }
}
