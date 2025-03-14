package ai.maum.chathub.api.document;

import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.meta.SecurityMeta;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.document.dto.req.DocumentLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final HttpSession session;
    private final DocumentUserRepository documentUserRepository;

    public void loginDoc(DocumentLoginRequest req) {
        DocumentUserEntity documentUser = documentUserRepository.findByAccountAndPassword(req.getAccount(), req.getPassword());
        if(documentUser == null) throw BaseException.of(ResponseMeta.WRONG_DOCUMENT_USER);
        session.setAttribute("user", req.getAccount());
        session.setMaxInactiveInterval(SecurityMeta.DOCUMENT_SESSION_EXPIRE_TIME);
    }
}
