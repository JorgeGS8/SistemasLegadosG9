      ******************************************************************
      * Author: Grupo9
      * Date: 22/09/2026
      * Purpose: Crear un archivo movimientos.ubd
      * Tectonics: cobc
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CREAR-MOVIMIENTOS.
       ENVIRONMENT DIVISION.

       CONFIGURATION SECTION.

       SPECIAL-NAMES.

           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.

           SELECT F-MOVIMIENTOS ASSIGN TO DISK
               ORGANIZATION IS INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS MOV-NUM
               FILE STATUS IS FSM.

           DATA DIVISION.
           FILE SECTION.

           FD F-MOVIMIENTOS
               LABEL RECORD STANDARD
               VALUE OF FILE-ID IS "movimientos.ubd".

           01 MOVIMIENTO-REG.

               02 MOV-NUM          PIC 9(35).
               02 MOV-TARJETA      PIC 9(16).
               02 MOV-ANO          PIC 9(4).
               02 MOV-MES          PIC 9(2).
               02 MOV-DIA          PIC 9(2).
               02 MOV-HOR          PIC 9(2).
               02 MOV-MIN          PIC 9(2).
               02 MOV-SEG          PIC 9(2).
               02 MOV-IMPORTE-ENT  PIC S9(7).
               02 MOV-IMPORTE-DEC  PIC 9(2).
               02 MOV-CONCEPTO     PIC X(35).
               02 MOV-SALDOPOS-ENT PIC S9(9).
               02 MOV-SALDOPOS-DEC PIC 9(2).

           WORKING-STORAGE SECTION.

           77 FSM                  PIC X(2).
           77 TARJETA-INTRODUCIDA  PIC 9(16).

           01 KEYBOARD-STATUS      PIC 9(4).
               88 ENTER-PRESSED    VALUE 0.
               88 ESC-PRESSED      VALUE 2005.

           SCREEN SECTION.

           01 DATA-ACCEPT.

               05 FILLER LINE 8 COL 15
                   VALUE "Numero de tarjeta:".

               05 TARJETA-ACCEPT BLANK ZERO AUTO
                   LINE 8 COL 40
                   PIC 9(16) USING TARJETA-INTRODUCIDA.

               05 FILLER LINE 24 COL 33
                   VALUE "Enter - Aceptar".

           PROCEDURE DIVISION.

           MAIN-PROCEDURE.

               DISPLAY "CREAR MOVIMIENTOS".

               DISPLAY DATA-ACCEPT.

               ACCEPT DATA-ACCEPT ON EXCEPTION
                   IF ESC-PRESSED
                       STOP RUN
                   END-IF.

               ACCEPT TARJETA-INTRODUCIDA.

               OPEN OUTPUT F-MOVIMIENTOS.

               IF FSM NOT = "00"
                   DISPLAY "ERROR AL CREAR movimientos.ubd"
                   DISPLAY "FILE STATUS: " FSM
                   STOP RUN
               END-IF.

               INITIALIZE MOVIMIENTO-REG.

               MOVE 1 TO MOV-NUM.
               MOVE TARJETA-INTRODUCIDA TO MOV-TARJETA.

               MOVE 2026 TO MOV-ANO.
               MOVE 9 TO MOV-MES.
               MOVE 22 TO MOV-DIA.
               MOVE 12 TO MOV-HOR.
               MOVE 0 TO MOV-MIN.
               MOVE 0 TO MOV-SEG.

               MOVE 1000 TO MOV-IMPORTE-ENT.
               MOVE 0 TO MOV-IMPORTE-DEC.

               MOVE "Saldo inicial" TO MOV-CONCEPTO.

               MOVE 1000 TO MOV-SALDOPOS-ENT.
               MOVE 0 TO MOV-SALDOPOS-DEC.

               WRITE MOVIMIENTO-REG.

               IF FSM NOT = "00"
                   DISPLAY "ERROR AL ESCRIBIR EL MOVIMIENTO"
                   DISPLAY "FILE STATUS: " FSM
                   CLOSE F-MOVIMIENTOS
                   STOP RUN
               END-IF.

               CLOSE F-MOVIMIENTOS.

               DISPLAY " ".
               DISPLAY "movimientos.ubd creado correctamente.".
               DISPLAY "Saldo inicial: 1000.00 EUR".

               STOP RUN.

       END PROGRAM CREAR-MOVIMIENTOS.
