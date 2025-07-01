package com.mayvel.pdfGenerator.Const;

public class Consts {
    public static int port = 5005;
    public static int HistoryPort = 9000;
    public static String host = "localhost";
    public static String web_key_header = "sec-websocket-key";
    public static String default_pattern = "dd-MMM-yy hh:mm:ss a z";

    public static String alarm_sync_all_route = "/alarm_syncall";
    public static String alarm_sync_route = "/alarm_sync";
    public static String history_sync_all_route = "/history_syncallTest";
    public static String history_sync_route = "/history_sync";
    public static String history_sync_names_route = "/history_sync_names";
    public static String history_generate_pdf_route = "/generatePdf";
    public static String get_all_pdfs_name_list = "/getAllPdfNameList";
    public static String delete_pdf_by_name = "/deletePdf";
    //public static String health_route = "/health";
}
