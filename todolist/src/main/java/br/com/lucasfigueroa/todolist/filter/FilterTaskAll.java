package br.com.lucasfigueroa.todolist.filter;

import br.com.lucasfigueroa.todolist.user.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAll extends OncePerRequestFilter {


    @Autowired
    private UserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")){
            var auth = request.getHeader("Authorization");
            var authEncoded = auth.substring("Basic".length()).trim();

            byte[] authDecode = Base64.getDecoder().decode(authEncoded);

            var authStr = new String(authDecode);

            String[] credentials = authStr.split(":");
            String username = credentials[0];
            String password = credentials[1];

            //Validar usuário

            var user = this.userRepository.findByUserName(username);
            if (user == null){
                response.sendError(401);
            }else {

                //Validar senha
                if (!password.equals(user.getPassword())){
                    response.sendError(401);
                }else {
                    //Aprovado
                    //request: vem da requisicao para controller
                    //response: é enviado (resposta)
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request,response);
                }

            }
        }else{
            filterChain.doFilter(request,response);
        }


        //Pegar autenticacao (user e senha)


    }
}
