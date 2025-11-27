package com.upb.snack.controller;

import com.upb.snack.entity.Menu;
import com.upb.snack.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    private final MenuService menuService;

    // Carpeta donde se guardan las im??genes (ruta absoluta al proyecto)
    private static final Path UPLOAD_DIR = Paths.get(
            System.getProperty("user.dir"),
            "src", "main", "resources", "static", "img"
    ).toAbsolutePath();

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // ----------- CRUD b??sico -----------

    @GetMapping
    public List<Menu> getAllMenuItems() {
        return menuService.getAllMenuItems();
    }

    @GetMapping("/{id}")
    public Menu getMenuItem(@PathVariable Long id) {
        return menuService.getMenuItem(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Menu createMenuItem(@RequestBody Menu menu) {
        return menuService.createMenuItem(menu);
    }

    @PutMapping("/{id}")
    public Menu updateMenuItem(@PathVariable Long id, @RequestBody Menu menu) {
        return menuService.updateMenuItem(id, menu);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
    }

    // ----------- POST /menu/with-image -----------

    @PostMapping("/with-image")
    @ResponseStatus(HttpStatus.CREATED)
    public Menu createMenuWithImage(
            @RequestParam("nombre") String nombre,
            @RequestParam("precio") BigDecimal precio,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestPart("imagen") MultipartFile imagen
    ) {
        log.info("POST /menu/with-image -> nombre='{}', precio={}, stock={}, descripcion='{}', imagen={}",
                nombre,
                precio,
                stock,
                descripcion,
                (imagen != null ? imagen.getOriginalFilename() : "null"));

        if (imagen == null || imagen.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        String originalFilename = StringUtils.cleanPath(imagen.getOriginalFilename());
        log.info("Nombre de archivo limpio='{}'", originalFilename);

        if (originalFilename.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Nombre de archivo inv??lido");
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        log.info("Extensi??n detectada='{}'", extension);

        if (extension == null ||
                !(extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo se permiten im??genes JPG/JPEG");
        }

        try {
            // Nos aseguramos de que la carpeta exista
            Files.createDirectories(UPLOAD_DIR);

            String newFileName = System.currentTimeMillis() + "-" + originalFilename;
            Path target = UPLOAD_DIR.resolve(newFileName);
            File destFile = target.toFile();

            log.info("Guardando archivo en ruta f??sica='{}'", destFile.getAbsolutePath());

            // Guardamos el archivo f??sico
            imagen.transferTo(destFile);

            // Construimos la entidad Menu
            Menu menu = new Menu();
            menu.setNombreProducto(nombre);
            menu.setPrecio(precio.doubleValue()); // ajusta si tu entidad usa BigDecimal
            menu.setStock(stock);
            menu.setDescripcion(descripcion);
            menu.setImageUrl("/img/" + newFileName); // ruta est??tica para el front
            menu.setEstado("ACTIVO");

            log.info("Creando registro Menu: nombre='{}', precio={}, stock={}, imageUrl='{}'",
                    menu.getNombreProducto(),
                    menu.getPrecio(),
                    menu.getStock(),
                    menu.getImageUrl());

            return menuService.createMenuItem(menu);

        } catch (IOException ex) {
            log.error("Error al guardar la imagen en el servidor", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al guardar la imagen en el servidor", ex
            );
        }
    }
}

