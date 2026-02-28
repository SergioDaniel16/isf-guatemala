package com.isf.guatemala.controller;

import com.isf.guatemala.model.*;
import com.isf.guatemala.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private RegistroHorasRepository registroHorasRepository;

    @Autowired
    private ViajeIniciadoRepository viajeIniciadoRepository;

    @Autowired
    private ViajeFinalizadoRepository viajeFinalizadoRepository;

    @GetMapping("/horas/excel")
    public ResponseEntity<byte[]> exportarHorasExcel(
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String fechaFin,
        @RequestParam(required = false) Long proyectoId,
        @RequestParam(required = false) Long empleadoId
    ) throws Exception {
        
        List<RegistroHoras> registros;
        
        if (fechaInicio != null && fechaFin != null) {
            registros = registroHorasRepository.findByFechaBetween(
                LocalDate.parse(fechaInicio), 
                LocalDate.parse(fechaFin)
            );
        } else if (proyectoId != null) {
            registros = registroHorasRepository.findByProyectoId(proyectoId);
        } else if (empleadoId != null) {
            registros = registroHorasRepository.findByEmpleadoId(empleadoId);
        } else {
            registros = registroHorasRepository.findAll();
        }

        // Crear workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Resumen de Horas");

        // Estilo para encabezados
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Fecha", "Empleado", "Proyecto", "Tarea", "Horas"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos
        int rowNum = 1;
        for (RegistroHoras registro : registros) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(registro.getFecha().toString());
            row.createCell(1).setCellValue(
                registro.getEmpleado().getPrimerNombre() + " " + 
                registro.getEmpleado().getPrimerApellido()
            );
            row.createCell(2).setCellValue(registro.getProyecto().getNombre());
            row.createCell(3).setCellValue(registro.getTarea().getNombre());
            row.createCell(4).setCellValue(registro.getHoras());
        }

        // Auto-ajustar columnas
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Convertir a bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Resumen_Horas.xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .body(outputStream.toByteArray());
    }

    @GetMapping("/kilometraje/excel")
    public ResponseEntity<byte[]> exportarKilometrajeExcel(
        @RequestParam String fechaInicio,
        @RequestParam String fechaFin,
        @RequestParam(required = false) Long proyectoId,
        @RequestParam(required = false) Long vehiculoId,
        @RequestParam(required = false) Long oficinaId,
        @RequestParam String vista
    ) throws Exception {
        
        List<ViajeIniciado> todosViajes = viajeIniciadoRepository.findAll();
        
        List<ViajeIniciado> viajesFiltrados = todosViajes.stream()
            .filter(v -> v.getEstado() == ViajeIniciado.Estado.FINALIZADO)
            .filter(v -> {
                LocalDate fecha = v.getFechaInicio();
                LocalDate inicio = LocalDate.parse(fechaInicio);
                LocalDate fin = LocalDate.parse(fechaFin);
                return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
            })
            .filter(v -> proyectoId == null || v.getProyecto().getId().equals(proyectoId))
            .filter(v -> vehiculoId == null || v.getVehiculo().getId().equals(vehiculoId))
            .filter(v -> oficinaId == null || v.getOficina().getId().equals(oficinaId))
            .collect(java.util.stream.Collectors.toList());

        List<ViajeFinalizado> finalizados = viajeFinalizadoRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        
        if ("proyecto".equals(vista)) {
            exportarVistaPorProyecto(workbook, viajesFiltrados, finalizados);
        } else {
            exportarVistaPorVehiculo(workbook, viajesFiltrados, finalizados);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Reporte_Kilometraje.xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .body(outputStream.toByteArray());
    }

    private void exportarVistaPorProyecto(Workbook workbook, List<ViajeIniciado> viajes, List<ViajeFinalizado> finalizados) {
        Sheet sheet = workbook.createSheet("Por Proyecto");

        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo para números con 2 decimales (formato: 0.00)
        CellStyle decimalStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        decimalStyle.setDataFormat(format.getFormat("0.00"));

        // Estilo para moneda USD (formato: $0.00)
        CellStyle currencyUSDStyle = workbook.createCellStyle();
        currencyUSDStyle.setDataFormat(format.getFormat("$#,##0.00"));

        // Estilo para moneda GTQ (formato: Q0.00)
        CellStyle currencyGTQStyle = workbook.createCellStyle();
        currencyGTQStyle.setDataFormat(format.getFormat("\"Q\"#,##0.00"));

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Proyecto", "Vehículo", "Días Base", "Tarifa", "KM Extra", "Tarifa KM Extra", "Costo USD", "Costo GTQ"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        java.util.Map<String, java.util.Map<String, Object>> porProyecto = new java.util.LinkedHashMap<>();

        for (ViajeIniciado viaje : viajes) {
            String proyectoNombre = viaje.getProyecto().getNombre();
            String vehiculoPlaca = viaje.getVehiculo().getPlaca();

            if (!porProyecto.containsKey(proyectoNombre)) {
                porProyecto.put(proyectoNombre, new java.util.HashMap<>());
            }

            java.util.Map<String, Object> proyecto = porProyecto.get(proyectoNombre);

            if (!proyecto.containsKey("vehiculos")) {
                proyecto.put("vehiculos", new java.util.LinkedHashMap<>());
                proyecto.put("tarifaBase", viaje.getProyecto().getTarifaBaseVehiculo());
                proyecto.put("tarifaKmExtra", viaje.getProyecto().getTarifaKmExtra());
            }

            @SuppressWarnings("unchecked")
            java.util.Map<String, java.util.Map<String, Object>> vehiculos = 
                (java.util.Map<String, java.util.Map<String, Object>>) proyecto.get("vehiculos");

            if (!vehiculos.containsKey(vehiculoPlaca)) {
                java.util.Map<String, Object> vehiculoData = new java.util.HashMap<>();
                vehiculoData.put("diasBase", 0);
                vehiculoData.put("kmExtra", 0.0);
                vehiculoData.put("costoUSD", 0.0);
                vehiculoData.put("costoGTQ", 0.0);
                vehiculos.put(vehiculoPlaca, vehiculoData);
            }

            ViajeFinalizado finalizado = finalizados.stream()
                .filter(vf -> vf.getViajeIniciado().getId().equals(viaje.getId()))
                .findFirst()
                .orElse(null);

            if (finalizado != null) {
                java.util.Map<String, Object> vehiculoData = vehiculos.get(vehiculoPlaca);
                vehiculoData.put("diasBase", (int) vehiculoData.get("diasBase") + 1);
                vehiculoData.put("kmExtra", (double) vehiculoData.get("kmExtra") + finalizado.getKmExtra());
                vehiculoData.put("costoUSD", (double) vehiculoData.get("costoUSD") + finalizado.getCostoUSD());
                vehiculoData.put("costoGTQ", (double) vehiculoData.get("costoGTQ") + finalizado.getCostoGTQ());
            }
        }

        for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : porProyecto.entrySet()) {
            String proyecto = entry.getKey();
            java.util.Map<String, Object> datos = entry.getValue();
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, java.util.Map<String, Object>> vehiculos = 
                (java.util.Map<String, java.util.Map<String, Object>>) datos.get("vehiculos");

            for (java.util.Map.Entry<String, java.util.Map<String, Object>> vehiculoEntry : vehiculos.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                java.util.Map<String, Object> vdata = vehiculoEntry.getValue();
                
                // Columna 0: Proyecto (texto)
                row.createCell(0).setCellValue(proyecto);
                
                // Columna 1: Vehículo (texto)
                row.createCell(1).setCellValue(vehiculoEntry.getKey());
                
                // Columna 2: Días Base (número entero)
                row.createCell(2).setCellValue((int) vdata.get("diasBase"));
                
                // Columna 3: Tarifa (número decimal)
                Cell tarifaCell = row.createCell(3);
                tarifaCell.setCellValue((double) datos.get("tarifaBase"));
                tarifaCell.setCellStyle(currencyUSDStyle);
                
                // Columna 4: KM Extra (número decimal)
                Cell kmExtraCell = row.createCell(4);
                kmExtraCell.setCellValue((double) vdata.get("kmExtra"));
                kmExtraCell.setCellStyle(decimalStyle);
                
                // Columna 5: Tarifa KM Extra (número decimal)
                Cell tarifaKmCell = row.createCell(5);
                tarifaKmCell.setCellValue((double) datos.get("tarifaKmExtra"));
                tarifaKmCell.setCellStyle(currencyUSDStyle);
                
                // Columna 6: Costo USD (moneda)
                Cell costoUSDCell = row.createCell(6);
                costoUSDCell.setCellValue((double) vdata.get("costoUSD"));
                costoUSDCell.setCellStyle(currencyUSDStyle);
                
                // Columna 7: Costo GTQ (moneda)
                Cell costoGTQCell = row.createCell(7);
                costoGTQCell.setCellValue((double) vdata.get("costoGTQ"));
                costoGTQCell.setCellStyle(currencyGTQStyle);
            }
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void exportarVistaPorVehiculo(Workbook workbook, List<ViajeIniciado> viajes, List<ViajeFinalizado> finalizados) {
        Sheet sheet = workbook.createSheet("Por Vehículo");

        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo para números con 2 decimales
        CellStyle decimalStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        decimalStyle.setDataFormat(format.getFormat("0.00"));

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Vehículo", "Millaje Inicial", "Millaje Final", "Total Recorrido"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        java.util.Map<String, java.util.Map<String, Object>> porVehiculo = new java.util.LinkedHashMap<>();

        for (ViajeIniciado viaje : viajes) {
            String vehiculoKey = viaje.getVehiculo().getPlaca() + " - " + 
                                viaje.getVehiculo().getMarca() + " " + 
                                viaje.getVehiculo().getModelo();

            if (!porVehiculo.containsKey(vehiculoKey)) {
                java.util.Map<String, Object> vehiculoData = new java.util.HashMap<>();
                vehiculoData.put("millageInicial", Double.MAX_VALUE);
                vehiculoData.put("millageFinal", 0.0);
                vehiculoData.put("totalRecorrido", 0.0);
                porVehiculo.put(vehiculoKey, vehiculoData);
            }

            java.util.Map<String, Object> vehiculoData = porVehiculo.get(vehiculoKey);

            ViajeFinalizado finalizado = finalizados.stream()
                .filter(vf -> vf.getViajeIniciado().getId().equals(viaje.getId()))
                .findFirst()
                .orElse(null);

            if (finalizado != null) {
                double millageInicial = Math.min((double) vehiculoData.get("millageInicial"), viaje.getMillajeSalida());
                double millageFinal = Math.max((double) vehiculoData.get("millageFinal"), finalizado.getMillajeEntrada());

                vehiculoData.put("millageInicial", millageInicial);
                vehiculoData.put("millageFinal", millageFinal);
                vehiculoData.put("totalRecorrido", millageFinal - millageInicial);
            }
        }

        int rowNum = 1;
        for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : porVehiculo.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            java.util.Map<String, Object> datos = entry.getValue();
            
            // Columna 0: Vehículo (texto)
            row.createCell(0).setCellValue(entry.getKey());
            
            // Columna 1: Millaje Inicial (número decimal)
            Cell millageInicialCell = row.createCell(1);
            double millageInicial = (double) datos.get("millageInicial");
            if (millageInicial != Double.MAX_VALUE) {
                millageInicialCell.setCellValue(millageInicial);
                millageInicialCell.setCellStyle(decimalStyle);
            } else {
                millageInicialCell.setCellValue("N/A");
            }
            
            // Columna 2: Millaje Final (número decimal)
            Cell millageFinalCell = row.createCell(2);
            millageFinalCell.setCellValue((double) datos.get("millageFinal"));
            millageFinalCell.setCellStyle(decimalStyle);
            
            // Columna 3: Total Recorrido (número decimal)
            Cell totalRecorridoCell = row.createCell(3);
            totalRecorridoCell.setCellValue((double) datos.get("totalRecorrido"));
            totalRecorridoCell.setCellStyle(decimalStyle);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
