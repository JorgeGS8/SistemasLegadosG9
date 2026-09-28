       IDENTIFICATION DIVISION.
       PROGRAM-ID. CAMBIAR-FECHAS-ESPECT.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT F-ESPECTACULOS ASSIGN TO DISK
               ORGANIZATION IS INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS ESP-NUM
               FILE STATUS IS FSE.

       DATA DIVISION.
       FILE SECTION.
       FD F-ESPECTACULOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "espectaculos.ubd".
       01 ESPECTACULO-REG.
           02 ESP-NUM               PIC 9(4).
           02 ESP-ANO               PIC 9(4).
           02 ESP-MES               PIC 9(2).
           02 ESP-DIA               PIC 9(2).
           02 ESP-HOR               PIC 9(2).
           02 ESP-MIN               PIC 9(2).
           02 ESP-DESCR             PIC X(40).
           02 ESP-DISP              PIC 9(7).
           02 ESP-PRECIO-ENT        PIC 9(4).
           02 ESP-PRECIO-DEC        PIC 9(2).

       WORKING-STORAGE SECTION.
       77 FSE                       PIC X(2).
       77 ESPECTACULOS-ACTUALIZADOS PIC 9(5) VALUE 0.

       01 CAMPOS-FECHA.
           05 FECHA.
               10 ANO               PIC 9(4).
               10 MES               PIC 9(2).
               10 DIA               PIC 9(2).
           05 HORA.
               10 HORAS             PIC 9(2).
               10 MINUTOS           PIC 9(2).
               10 SEGUNDOS          PIC 9(2).
               10 MILISEGUNDOS      PIC 9(2).
           05 DIF-GMT               PIC S9(4).

       PROCEDURE DIVISION.
       PRINCIPAL.
           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           OPEN I-O F-ESPECTACULOS.
           IF FSE NOT = "00"
               DISPLAY "ERROR AL ABRIR espectaculos.ubd: " FSE
               STOP RUN
           END-IF.

       LEER-ESPECTACULO.
           READ F-ESPECTACULOS NEXT RECORD
               AT END GO TO FIN-ACTUALIZACION.

           MOVE 2027 TO ESP-ANO.

           REWRITE ESPECTACULO-REG
               INVALID KEY
                   DISPLAY "ERROR AL REESCRIBIR: " ESP-NUM
                   DISPLAY "CODIGO: " FSE
                   CLOSE F-ESPECTACULOS
                   STOP RUN
           END-REWRITE.

           ADD 1 TO ESPECTACULOS-ACTUALIZADOS.
           GO TO LEER-ESPECTACULO.

       FIN-ACTUALIZACION.
           CLOSE F-ESPECTACULOS.
           DISPLAY "FECHA ASIGNADA: " ESP-ANO "/" ESP-MES "/" ESP-DIA.
           DISPLAY "ESPECTACULOS ACTUALIZADOS: "
               ESPECTACULOS-ACTUALIZADOS.
           STOP RUN.
