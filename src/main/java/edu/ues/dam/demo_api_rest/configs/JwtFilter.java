package edu.ues.dam.demo_api_rest.configs;

import edu.ues.dam.demo_api_rest.security.JwtUtil;
import edu.ues.dam.demo_api_rest.configs.CustomerDetailServices;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomerDetailServices customerDetailServices; // inyectado para obtener authorities

    @PostConstruct
    public void init() {
        System.out.println(">>> JwtFilter: @PostConstruct called - bean inicializado");
        LOG.info(">>> JwtFilter: bean inicializado");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // DIAGNOSTICO: loguear siempre al entrar
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        LOG.info(">>> JwtFilter ENTER: method={}, uri={}, contextPath={}, servletPath={}", method, uri, contextPath, servletPath);
        System.out.println(">>> JwtFilter ENTER: method=" + method + " uri=" + uri + " contextPath=" + contextPath + " servletPath=" + servletPath);

        // --- CORS headers básicos ---
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Access-Control-Max-Age", "3600");

        // Preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Obtener context path y URI y sacar solo la parte relativa a la app
        String path = uri;
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            path = uri.substring(contextPath.length());     // ej: /auth/login
        }
        LOG.info(">>> JwtFilter relative path='{}'", path);

        // RUTAS PÚBLICAS (no requieren token)
        if (path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/verify-token")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/")
                || path.startsWith("/actuator/")) {
            LOG.info(">>> JwtFilter allowing public path '{}'", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Validación del header Authorization
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7).trim();
            LOG.info(">>> JwtFilter found Bearer token, length={}", token.length());
        } else {
            LOG.info(">>> JwtFilter no Bearer token present (header={})", authorizationHeader);
        }

        try {
            if (token != null && jwtUtil.validatedTokenPermission(token)) {
                // --- NUEVO: poblar SecurityContext con Authentication ---
                String username = jwtUtil.getUsernameFromToken(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customerDetailServices.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    LOG.info(">>> JwtFilter token validated and authentication set for user '{}'", username);
                } else {
                    LOG.debug(">>> JwtFilter username is null or authentication already present");
                }
                // continuar la cadena
                filterChain.doFilter(request, response);
            } else {
                LOG.warn(">>> JwtFilter token invalid or missing; returning 401");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("No autorizado: Token no es el correcto o no proporcionado");
            }
        } catch (Exception ex) {
            LOG.error(">>> JwtFilter exception validating token: {}", ex.getMessage(), ex);
            // limpiar contexto por seguridad
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error interno al validar token");
        }
    }
}
