package br.com.kelvinnielson.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;

import br.com.kelvinnielson.todolist.user.IUserRepository;

import java.io.IOException;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
            // Pegar a autenticação (usuário e senha)
            var authorization = request.getHeader("Authorization");

            // Encodar em Base64 e retirar as informações desnecessárias
            var authEncoded = authorization.substring("Basic".length()).trim();

            // Descodar o Base64 para Byte
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);

            // Transformar o Byte em String
            var authString = new String(authDecode);

            // ["kelvinnielson", "teste"]
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            // Validar usuário
            var user = this.userRepository.findByUsername(username);

            // Se o usuário não existir no banco de dados, emitir um erro
            if (user == null) {
                response.sendError(401);
            } else {
                // Se tudo correr bem, vamos validar a senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                // Se a senha verificada for correta, seguir viagem
                if (passwordVerify.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                		// Se a senha verificada for incorreta, emitir um erro
                    response.sendError(401);
                }

            }
        } else {
            filterChain.doFilter(request, response);
        }

    }

}
