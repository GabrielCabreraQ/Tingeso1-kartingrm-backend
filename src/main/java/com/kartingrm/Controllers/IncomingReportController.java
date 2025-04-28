package com.kartingrm.Controllers;

import com.kartingrm.Entities.IncomingReportEntity;
import com.kartingrm.Services.IncomingReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/report")
@CrossOrigin("*")
public class IncomingReportController {
    @Autowired
    IncomingReportService incomingReportService;

    @GetMapping("/")
    public ResponseEntity<List<IncomingReportEntity>> listReports() {
        List<IncomingReportEntity> reports = incomingReportService.getAllIncomingReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) { // Cambia el tipo de retorno a byte[]
        // Asegúrate que tu servicio devuelva el IncomingReportEntity
        // o al menos el byte[] y el filename.
        // Una buena práctica es que el servicio devuelva la entidad completa o un Optional.
        Optional<IncomingReportEntity> reportOptional = Optional.ofNullable(incomingReportService.getIncomingReportById(id)); // Asumiendo que tienes este método

        if (reportOptional.isPresent()) {
            IncomingReportEntity report = reportOptional.get();
            byte[] fileContent = report.getFileContent();

            // Verifica si el contenido del archivo es nulo o vacío
            if (fileContent == null || fileContent.length == 0) {
                // Reporte encontrado, pero sin contenido de archivo
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Puedes devolver un cuerpo vacío o un mensaje de error si la respuesta no es byte[]
            }

            HttpHeaders headers = new HttpHeaders();
            // Asegúrate de que el Content-Type coincida con el tipo de archivo (xlsx)
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            // Usa el nombre de archivo guardado en la entidad
            String fileName = report.getFileName() != null && !report.getFileName().isEmpty() ? report.getFileName() : "reporte_" + id + ".xlsx";
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(fileContent.length); // Establece la longitud del contenido

            // Devuelve directamente el byte[] en el cuerpo de la respuesta
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } else {
            // Reporte no encontrado
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // O puedes devolver un mensaje de error si la respuesta no es byte[]
        }
    }


    // --- Endpoint para generar reporte por Vueltas (modificado para devolver IncomingReportEntity) ---
    @PostMapping("/laps/{startdate}/{enddate}")
    public ResponseEntity<IncomingReportEntity> generateIncomeReportByLaps( // Cambia el tipo de retorno a IncomingReportEntity
                                                                            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startdate,
                                                                            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enddate) { // Elimina 'throws IOException' si tu servicio lo maneja internamente

        try {
            // *** IMPORTANTE: Tu método de servicio debe ahora generar el reporte, guardarlo y RETORNAR el IncomingReportEntity guardado ***
            IncomingReportEntity savedReport = incomingReportService.generateIncomeReportByLaps(startdate, enddate); // <--- Asumiendo que tu servicio tiene un método así

            // Devuelve la entidad guardada en el cuerpo de la respuesta con estado 201 Created
            // Spring Boot/Jackson se encargará de convertir el objeto Java a JSON
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);

        } catch (Exception e) { // Captura excepciones más genéricas si tu servicio lanza varias
            // Loguea el error en el servidor
            e.printStackTrace();
            // Devuelve una respuesta de error (ej. 500 Internal Server Error)
            // Considera devolver un cuerpo de error JSON con detalles si es necesario para el frontend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // O .body("Mensaje de error");
        }
    }

    @PostMapping("/group/{startdate}/{enddate}")
    public ResponseEntity<IncomingReportEntity> generateIncomeReportByGroupSize( // Cambia el tipo de retorno a IncomingReportEntity
                                                                                 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startdate,
                                                                                 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enddate) { // Elimina 'throws IOException' si tu servicio lo maneja

        try {
            // *** IMPORTANTE: Tu método de servicio debe ahora generar el reporte, guardarlo y RETORNAR el IncomingReportEntity guardado ***
            IncomingReportEntity savedReport = incomingReportService.generateIncomeReportByGroupSize(startdate, enddate); // <--- Asumiendo que tu servicio tiene un método así

            // Devuelve la entidad guardada en el cuerpo de la respuesta con estado 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);

        } catch (Exception e) { // Captura excepciones más genéricas
            // Loguea el error en el servidor
            e.printStackTrace();
            // Devuelve una respuesta de error (ej. 500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // O .body("Mensaje de error");
        }
    }

}
