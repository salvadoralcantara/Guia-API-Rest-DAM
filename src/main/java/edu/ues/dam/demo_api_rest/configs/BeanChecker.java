package edu.ues.dam.demo_api_rest.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanChecker implements CommandLineRunner {

    private final ApplicationContext ctx;

    public BeanChecker(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> BeanChecker: buscando beans con 'jwt' en el contexto:");
        String[] names = ctx.getBeanDefinitionNames();
        for (String n : names) {
            if (n.toLowerCase().contains("jwt")) {
                System.out.println(" - " + n + " => " + ctx.getBean(n).getClass().getName());
            }
        }
    }
}
