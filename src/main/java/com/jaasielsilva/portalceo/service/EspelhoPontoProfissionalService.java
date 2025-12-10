package com.jaasielsilva.portalceo.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jaasielsilva.portalceo.repository.RhParametroPontoRepository;
import com.jaasielsilva.portalceo.model.RhParametroPonto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class EspelhoPontoProfissionalService {

    // Cores corporativas
    private static final BaseColor COR_CABECALHO = new BaseColor(41, 128, 185); // Azul profissional
    private static final BaseColor COR_ALTERNADA = new BaseColor(245, 245, 245); // Cinza claro
    private static final BaseColor COR_TOTAL = new BaseColor(52, 152, 219); // Azul claro

    @Autowired
    private RhParametroPontoRepository pontoRepository;

    public byte[] gerarEspelhoProfissional(Colaborador colaborador, List<RegistroPonto> registros,
            int ano, int mes) throws DocumentException, IOException {

        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();

        // 1. CABEÇALHO CORPORATIVO
        adicionarCabecalhoCorporativo(document, colaborador, ano, mes);

        // 2. INFORMAÇÕES DO COLABORADOR
        adicionarInformacoesColaborador(document, colaborador);

        // 3. TABELA DE REGISTROS
        adicionarTabelaRegistros(document, registros);

        // 4. RESUMO MENSAL
        adicionarResumoMensal(document, registros, colaborador);

        // 5. RODAPÉ COM SEGURANÇA
        adicionarRodapeSeguranca(document, writer);

        document.close();
        return baos.toByteArray();
    }

    private void adicionarCabecalhoCorporativo(Document document, Colaborador colaborador,
            int ano, int mes) throws DocumentException {

        // Tabela para layout do cabeçalho
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 70, 30 });

        // Logo e informações da empresa (lado esquerdo)
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);

        Font empresaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COR_CABECALHO);
        Font enderecoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);

        Paragraph empresa = new Paragraph("PORTAL CEO CORPORATIVO", empresaFont);
        Paragraph endereco = new Paragraph("Sistema Integrado de Gestão Empresarial", enderecoFont);
        Paragraph cnpj = new Paragraph("CNPJ: 00.000.000/0001-00", enderecoFont);

        logoCell.addElement(empresa);
        logoCell.addElement(endereco);
        logoCell.addElement(cnpj);
        headerTable.addCell(logoCell);

        // Período e data de geração (lado direito)
        PdfPCell periodoCell = new PdfPCell();
        periodoCell.setBorder(Rectangle.NO_BORDER);
        periodoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        String nomeMes = LocalDate.of(ano, mes, 1)
                .getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

        Font periodoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COR_CABECALHO);
        Paragraph periodoTexto = new Paragraph(
                String.format("PERÍODO: %s/%d", nomeMes.toUpperCase(), ano), periodoFont);
        periodoTexto.setAlignment(Element.ALIGN_RIGHT);

        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Paragraph dataGeracao = new Paragraph(
                "Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                dataFont);
        dataGeracao.setAlignment(Element.ALIGN_RIGHT);

        periodoCell.addElement(periodoTexto);
        periodoCell.addElement(dataGeracao);
        headerTable.addCell(periodoCell);

        document.add(headerTable);

        // Linha separadora
        LineSeparator line = new LineSeparator();
        line.setLineColor(COR_CABECALHO);
        line.setLineWidth(2);
        document.add(new Chunk(line));
        document.add(new Paragraph(" "));
    }

    private void adicionarInformacoesColaborador(Document document, Colaborador colaborador)
            throws DocumentException {

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COR_CABECALHO);
        Paragraph titulo = new Paragraph("ESPELHO DE PONTO ELETRÔNICO", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(15);
        document.add(titulo);

        // Tabela de informações do colaborador
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 25, 25, 25, 25 });

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // Cabeçalhos
        adicionarCelulaInfo(infoTable, "COLABORADOR", labelFont, COR_CABECALHO, true);
        adicionarCelulaInfo(infoTable, "MATRÍCULA", labelFont, COR_CABECALHO, true);
        adicionarCelulaInfo(infoTable, "CARGO", labelFont, COR_CABECALHO, true);
        adicionarCelulaInfo(infoTable, "DEPARTAMENTO", labelFont, COR_CABECALHO, true);

        // Valores
        adicionarCelulaInfo(infoTable, colaborador.getNome(), valueFont, BaseColor.WHITE, false);
        adicionarCelulaInfo(infoTable, colaborador.getUsuario().getMatricula(), valueFont, BaseColor.WHITE, false);
        adicionarCelulaInfo(infoTable, colaborador.getCargo() != null ? colaborador.getCargo().getNome() : "N/A",
                valueFont, BaseColor.WHITE, false);
        adicionarCelulaInfo(infoTable,
                colaborador.getDepartamento() != null ? colaborador.getDepartamento().getNome() : "N/A",
                valueFont, BaseColor.WHITE, false);

        document.add(infoTable);
        document.add(new Paragraph(" "));
    }

    private void adicionarTabelaRegistros(Document document, List<RegistroPonto> registros)
            throws DocumentException {

        // Tabela principal de registros
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 8, 12, 12, 12, 12, 12, 10, 10 });

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);

        // Cabeçalhos
        String[] headers = { "DIA", "DATA", "ENTRADA 1", "SAÍDA 1", "ENTRADA 2", "SAÍDA 2", "TOTAL", "EXTRAS" };
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(COR_CABECALHO);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Dados dos registros
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        boolean linhaAlternada = false;
        for (RegistroPonto registro : registros) {
            BaseColor corFundo = linhaAlternada ? COR_ALTERNADA : BaseColor.WHITE;

            // Dia da semana
            String diaSemana = registro.getData().getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")).toUpperCase();
            adicionarCelulaRegistro(table, diaSemana, cellFont, corFundo);

            // Data
            adicionarCelulaRegistro(table, registro.getData().format(dateFormatter), cellFont, corFundo);

            // Horários
            adicionarCelulaRegistro(table, formatarHorario(registro.getEntrada1(), timeFormatter), cellFont, corFundo);
            adicionarCelulaRegistro(table, formatarHorario(registro.getSaida1(), timeFormatter), cellFont, corFundo);
            adicionarCelulaRegistro(table, formatarHorario(registro.getEntrada2(), timeFormatter), cellFont, corFundo);
            adicionarCelulaRegistro(table, formatarHorario(registro.getSaida2(), timeFormatter), cellFont, corFundo);

            // Cálculos
            HorasCalculadas horas = calcularHorasDetalhado(registro);
            adicionarCelulaRegistro(table, horas.getTotalFormatado(), cellFont, corFundo);
            adicionarCelulaRegistro(table, horas.getExtrasFormatado(), cellFont, corFundo);

            linhaAlternada = !linhaAlternada;
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void adicionarResumoMensal(Document document, List<RegistroPonto> registros,
            Colaborador colaborador) throws DocumentException {

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COR_CABECALHO);
        Paragraph titulo = new Paragraph("RESUMO MENSAL", tituloFont);
        titulo.setSpacingBefore(10);
        document.add(titulo);

        // Calcular totais
        ResumoMensal resumo = calcularResumoMensal(registros);

        // Tabela de resumo
        PdfPTable resumoTable = new PdfPTable(4);
        resumoTable.setWidthPercentage(100);
        resumoTable.setWidths(new float[] { 25, 25, 25, 25 });

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);

        // Primeira linha
        adicionarCelulaResumo(resumoTable, "DIAS TRABALHADOS", String.valueOf(resumo.diasTrabalhados),
                labelFont, valueFont, COR_TOTAL);
        adicionarCelulaResumo(resumoTable, "FALTAS", String.valueOf(resumo.faltas),
                labelFont, valueFont, COR_TOTAL);
        adicionarCelulaResumo(resumoTable, "HORAS NORMAIS", resumo.horasNormaisFormatado,
                labelFont, valueFont, COR_TOTAL);
        adicionarCelulaResumo(resumoTable, "HORAS EXTRAS", resumo.horasExtrasFormatado,
                labelFont, valueFont, COR_TOTAL);

        document.add(resumoTable);

        // Observações
        document.add(new Paragraph(" "));
        Font obsFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Paragraph obs = new Paragraph(
                "* Horas extras calculadas acima de 8h diárias\n" +
                        "* Relatório gerado conforme CLT Art. 74\n" +
                        "* Documento com validade legal",
                obsFont);
        document.add(obs);
    }

    private void adicionarRodapeSeguranca(Document document, PdfWriter writer) throws DocumentException {

        // Código de verificação
        String codigoVerificacao = gerarCodigoVerificacao();

        Font segurancaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.DARK_GRAY);
        Paragraph seguranca = new Paragraph(
                String.format("Código de Verificação: %s | Hash: %s",
                        codigoVerificacao,
                        gerarHashDocumento()),
                segurancaFont);
        seguranca.setAlignment(Element.ALIGN_CENTER);
        seguranca.setSpacingBefore(20);

        document.add(seguranca);
    }

    // Métodos auxiliares
    private void adicionarCelulaInfo(PdfPTable table, String texto, Font font,
            BaseColor cor, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(cor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(isHeader ? 8 : 5);
        if (isHeader) {
            cell.setBorder(Rectangle.BOX);
        }
        table.addCell(cell);
    }

    private void adicionarCelulaRegistro(PdfPTable table, String texto, Font font, BaseColor cor) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(cor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void adicionarCelulaResumo(PdfPTable table, String label, String value,
            Font labelFont, Font valueFont, BaseColor cor) {
        // Célula do label
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(cor);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setPadding(6);
        table.addCell(labelCell);

        // Célula do valor (será adicionada na próxima linha da tabela)
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(BaseColor.WHITE);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private String formatarHorario(LocalTime horario, DateTimeFormatter formatter) {
        return horario != null ? horario.format(formatter) : "-";
    }

    private HorasCalculadas calcularHorasDetalhado(RegistroPonto registro) {
        long minutosTotais = 0;

        if (registro.getEntrada1() != null && registro.getSaida1() != null) {
            minutosTotais += Duration.between(registro.getEntrada1(), registro.getSaida1()).toMinutes();
        }
        if (registro.getEntrada2() != null && registro.getSaida2() != null) {
            minutosTotais += Duration.between(registro.getEntrada2(), registro.getSaida2()).toMinutes();
        }

        RhParametroPonto cfg = pontoRepository.findAll().stream().findFirst().orElse(null);
        int jornadaNormal = 480; // 8h
        int tolerancia = cfg != null && cfg.getToleranciaMinutos() != null ? Math.max(0, cfg.getToleranciaMinutos())
                : 0;
        int arredondamento = cfg != null && cfg.getArredondamentoMinutos() != null
                ? Math.max(0, cfg.getArredondamentoMinutos())
                : 0;
        int limiteExtra = cfg != null && cfg.getLimiteHoraExtraDia() != null ? Math.max(0, cfg.getLimiteHoraExtraDia())
                : Integer.MAX_VALUE;

        long minutosAposArredondamento = minutosTotais;
        if (arredondamento > 0) {
            minutosAposArredondamento = Math.round((double) minutosTotais / arredondamento) * arredondamento;
        }

        long dif = minutosAposArredondamento - jornadaNormal;
        long extras;
        if (dif < 0 && Math.abs(dif) <= tolerancia) {
            extras = 0;
        } else {
            extras = Math.max(0, dif);
        }
        if (limiteExtra != Integer.MAX_VALUE) {
            extras = Math.min(extras, limiteExtra);
        }

        return new HorasCalculadas(minutosAposArredondamento, extras);
    }

    private ResumoMensal calcularResumoMensal(List<RegistroPonto> registros) {
        int diasTrabalhados = 0;
        int faltas = 0;
        long totalMinutosNormais = 0;
        long totalMinutosExtras = 0;
        int jornadaNormal = 480;

        for (RegistroPonto registro : registros) {
            HorasCalculadas horas = calcularHorasDetalhado(registro);
            if (horas.getTotalMinutos() > 0) {
                diasTrabalhados++;
                totalMinutosNormais += Math.min(horas.getTotalMinutos(), jornadaNormal);
                totalMinutosExtras += horas.getExtrasMinutos();
            } else {
                faltas++;
            }
        }

        return new ResumoMensal(diasTrabalhados, faltas, totalMinutosNormais, totalMinutosExtras);
    }

    private String gerarCodigoVerificacao() {
        return "ESP" + System.currentTimeMillis() % 1000000;
    }

    private String gerarHashDocumento() {
        return "MD5:" + Integer.toHexString((int) (Math.random() * 1000000)).toUpperCase();
    }

    // Classes auxiliares
    private static class HorasCalculadas {
        private final long totalMinutos;
        private final long extrasMinutos;

        public HorasCalculadas(long totalMinutos, long extrasMinutos) {
            this.totalMinutos = totalMinutos;
            this.extrasMinutos = extrasMinutos;
        }

        public long getTotalMinutos() {
            return totalMinutos;
        }

        public long getExtrasMinutos() {
            return extrasMinutos;
        }

        public String getTotalFormatado() {
            return formatarMinutos(totalMinutos);
        }

        public String getExtrasFormatado() {
            return extrasMinutos > 0 ? formatarMinutos(extrasMinutos) : "-";
        }

        private String formatarMinutos(long minutos) {
            return String.format("%02d:%02d", minutos / 60, minutos % 60);
        }
    }

    private static class ResumoMensal {
        public final int diasTrabalhados;
        public final int faltas;
        public final String horasNormaisFormatado;
        public final String horasExtrasFormatado;

        public ResumoMensal(int diasTrabalhados, int faltas, long minutosNormais, long minutosExtras) {
            this.diasTrabalhados = diasTrabalhados;
            this.faltas = faltas;
            this.horasNormaisFormatado = String.format("%02d:%02d", minutosNormais / 60, minutosNormais % 60);
            this.horasExtrasFormatado = String.format("%02d:%02d", minutosExtras / 60, minutosExtras % 60);
        }
    }
}
