package Solver; 

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileParser {

    public static Board parseFile(String filePath) throws IOException {
        int boardRows = 0;
        int boardCols = 0;

        List<String> pieceConfigLines = new ArrayList<>();
        int exitX = -1; 
        int exitY = -1; 
        boolean kProcessedBySpecificLogic = false; 

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null) throw new IOException("Format file tidak valid: Baris dimensi hilang.");
            String[] dimensions = line.split(" ");
            if (dimensions.length != 2) throw new IOException("Format file tidak valid: Dimensi salah.");
            try {
                boardRows = Integer.parseInt(dimensions[0]);
                boardCols = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                throw new IOException("Format file tidak valid: Dimensi bukan angka.");
            }

            line = reader.readLine(); 
            if (line == null) throw new IOException("Format file tidak valid: Baris N hilang.");

            List<String> allRawLines = new ArrayList<>();
            
            for (int i = 0; i < boardRows + 1; i++) {
                line = reader.readLine();
                if (line == null) {
                    if (i < boardRows) { 
                        throw new IOException("Format file tidak valid: Baris konfigurasi kurang. Diharapkan setidaknya " + boardRows + ", terbaca " + i + " setelah N.");
                    }
                    break; 
                }
                allRawLines.add(line);
            }

            if (allRawLines.size() == boardRows + 1) {
                String firstLine = allRawLines.get(0);
                String lastLine = allRawLines.get(boardRows); 

                if (firstLine.trim().equals("K")) {
                    int kColInLine = firstLine.indexOf('K');
                    if (kColInLine >= 0 && kColInLine < boardCols) {
                        exitY = 0; 
                        exitX = kColInLine;
                        for (int i = 1; i <= boardRows; i++) { 
                            String pLine = allRawLines.get(i);
                            if (pLine.length() != boardCols) throw new IOException("Format K ATAS: baris piece '" + pLine + "' panjangnya bukan " + boardCols);
                            pieceConfigLines.add(pLine);
                        }
                        kProcessedBySpecificLogic = true;
                    }
                }
                
                if (!kProcessedBySpecificLogic && lastLine.trim().equals("K")) {
                    int kColInLine = lastLine.indexOf('K');
                    if (kColInLine >= 0 && kColInLine < boardCols) {
                        exitY = boardRows - 1; 
                        exitX = kColInLine;
                        for (int i = 0; i < boardRows; i++) { 
                             String pLine = allRawLines.get(i);
                            if (pLine.length() != boardCols) throw new IOException("Format K BAWAH: baris piece '" + pLine + "' panjangnya bukan " + boardCols);
                            pieceConfigLines.add(pLine);
                        }
                        kProcessedBySpecificLogic = true;
                    }
                }
            }

            if (!kProcessedBySpecificLogic && allRawLines.size() >= boardRows) {
                List<String> horizontalCandidateLines = allRawLines.subList(0, boardRows);
                int kDefiningRow = -1; 
                boolean kModeIsLeft = false; 

                for (int r = 0; r < boardRows; r++) {
                    String currentLine = horizontalCandidateLines.get(r);
                    if (currentLine.length() == boardCols + 1) {
                        boolean startsWithK = currentLine.startsWith("K") && currentLine.substring(1).indexOf('K') == -1; // K unik di awal
                        boolean endsWithK = currentLine.endsWith("K") && currentLine.substring(0, boardCols).indexOf('K') == -1; // K unik di akhir

                        if (startsWithK) {
                            if (kDefiningRow != -1 && !kModeIsLeft) throw new IOException("Konflik definisi K: Kiri terdeteksi setelah K Kanan ditetapkan atau K didefinisikan di banyak tempat.");
                            if (kDefiningRow == -1) { // Hanya set jika ini definisi K pertama
                                exitX = 0; exitY = r;
                                kModeIsLeft = true; kDefiningRow = r;
                            }
                        } else if (endsWithK) {
                            if (kDefiningRow != -1 && kModeIsLeft) throw new IOException("Konflik definisi K: Kanan terdeteksi setelah K Kiri ditetapkan atau K didefinisikan di banyak tempat.");
                            if (kDefiningRow == -1) { // Hanya set jika ini definisi K pertama
                                exitX = boardCols - 1; exitY = r;
                                kModeIsLeft = false; // Berarti Kanan
                                kDefiningRow = r;
                            }
                        }
                    }
                }

                if (kDefiningRow != -1) { // Jika K Kiri atau Kanan terdefinisi dari satu baris
                    for (int r_fill = 0; r_fill < boardRows; r_fill++) {
                        String currentLineToProcess = horizontalCandidateLines.get(r_fill);
                        String lineToAdd;

                        if (currentLineToProcess.length() == boardCols + 1) {
                            if (kModeIsLeft) {
                                lineToAdd = currentLineToProcess.substring(1);
                            } else { 
                                lineToAdd = currentLineToProcess.substring(0, boardCols);
                            }
                        } else if (currentLineToProcess.length() == boardCols) {
                            lineToAdd = currentLineToProcess;
                        } else {
                            throw new IOException("Format K Kiri/Kanan tidak konsisten: baris '" + currentLineToProcess + "' memiliki panjang " + currentLineToProcess.length() + ", diharapkan " + boardCols + " atau " + (boardCols + 1));
                        }
                        
                        if (lineToAdd.length() != boardCols) {
                             throw new IOException("Internal error: Pemrosesan baris K Kiri/Kanan untuk '"+currentLineToProcess+"' menghasilkan '"+lineToAdd+"' dengan panjang salah.");
                        }
                        pieceConfigLines.add(lineToAdd);
                    }
                    kProcessedBySpecificLogic = true;
                }
            }


            if (!kProcessedBySpecificLogic) {
                throw new IOException("Format pintu keluar 'K' tidak dikenali atau file konfigurasi tidak lengkap/valid.");
            }
            
            if (pieceConfigLines.size() != boardRows) {
                 throw new IOException("Internal error: Jumlah baris piece (" + pieceConfigLines.size() + ") tidak sesuai dimensi (" + boardRows + ").");
            }
            for(String pLineVal : pieceConfigLines){
                if(pLineVal.length() != boardCols){ // Semua baris di pieceConfigLines harus sudah boardCols
                    throw new IOException("Internal error: Baris piece dalam pieceConfigLines ('" + pLineVal + "') panjangnya bukan " + boardCols);
                }
            }

            char[][] charGrid = new char[boardRows][boardCols];
            for (int r_cg = 0; r_cg < boardRows; r_cg++) {
                String pLineVal_cg = pieceConfigLines.get(r_cg);
                for (int c_cg = 0; c_cg < boardCols; c_cg++) {
                    charGrid[r_cg][c_cg] = pLineVal_cg.charAt(c_cg);
                    if (charGrid[r_cg][c_cg] == 'K') { // K tidak boleh ada di dalam board piece
                        throw new IOException("Format file tidak valid: 'K' ditemukan di dalam area piece setelah diproses, seharusnya di luar.");
                    }
                }
            }

            Map<Character, Piece> identifiedPieces = new HashMap<>();
            Set<Character> processedPieceIds = new HashSet<>();
             for (int r_ip = 0; r_ip < boardRows; r_ip++) {
                for (int c_ip = 0; c_ip < boardCols; c_ip++) {
                    char cellChar = charGrid[r_ip][c_ip];
                    if (cellChar != '.' && !processedPieceIds.contains(cellChar)) {
                        char pieceId = cellChar;
                        int pieceLength = 0;
                        Orientation orientation;
                        boolean isPrimary = (pieceId == 'P');

                        if (c_ip + 1 < boardCols && charGrid[r_ip][c_ip + 1] == pieceId) { 
                            orientation = Orientation.HORIZONTAL;
                            int currentC = c_ip;
                            while (currentC < boardCols && charGrid[r_ip][currentC] == pieceId) {
                                pieceLength++;
                                currentC++;
                            }
                        } else if (r_ip + 1 < boardRows && charGrid[r_ip + 1][c_ip] == pieceId) { 
                            orientation = Orientation.VERTICAL;
                            int currentR = r_ip;
                            while (currentR < boardRows && charGrid[currentR][c_ip] == pieceId) {
                                pieceLength++;
                                currentR++;
                            }
                        } else { 
                            orientation = Orientation.HORIZONTAL; // Default
                            pieceLength = 1;
                        }
                        
                        if (pieceLength > 0) {
                            if (orientation == Orientation.HORIZONTAL && pieceLength == 1 &&
                                r_ip + 1 < boardRows && charGrid[r_ip + 1][c_ip] == pieceId) {
                                int tempVerticalLength = 0;
                                int tempR = r_ip;
                                while(tempR < boardRows && charGrid[tempR][c_ip] == pieceId) {
                                    tempVerticalLength++;
                                    tempR++;
                                }
                                if (tempVerticalLength > pieceLength) { 
                                    orientation = Orientation.VERTICAL;
                                    pieceLength = tempVerticalLength;
                                }
                            }

                            if (pieceLength == 1 && pieceId != 'P') { 
                                System.out.println("Info: Piece '" + pieceId + "' di ("+r_ip+","+c_ip+") memiliki panjang 1.");
                            }
                            Piece piece = new Piece(pieceId, c_ip, r_ip, pieceLength, orientation, isPrimary);
                            identifiedPieces.put(pieceId, piece);
                            processedPieceIds.add(pieceId);
                        } else { 
                             throw new IOException("Error identifikasi piece: Piece '" + pieceId + "' di ("+r_ip+","+c_ip+") terdeteksi dengan panjang nol.");
                        }
                    }
                }
            }

            if (exitX == -1 || exitY == -1) { 
                 throw new IOException("Pintu keluar 'K' tidak terdefinisi setelah semua proses parsing.");
            }
            Piece primaryPiece = identifiedPieces.get('P');
            if (primaryPiece == null) {
                throw new IOException("Primary piece 'P' tidak ditemukan dalam konfigurasi.");
            }
            if (primaryPiece.getOrientation() == Orientation.HORIZONTAL) {
                if (!((exitX == 0 || exitX == boardCols - 1) && exitY == primaryPiece.getY() )) {
                     throw new IOException("Pintu keluar K ("+exitY+","+exitX+") tidak valid untuk Primary Piece 'P' H ("+primaryPiece.getY()+","+primaryPiece.getX()+"). Harusnya Y="+primaryPiece.getY()+", X=0 atau "+(boardCols-1)+".");
                }
            } else { 
                if (!((exitY == 0 || exitY == boardRows - 1) && exitX == primaryPiece.getX() )) {
                    throw new IOException("Pintu keluar K ("+exitY+","+exitX+") tidak valid untuk Primary Piece 'P' V ("+primaryPiece.getY()+","+primaryPiece.getX()+"). Harusnya X="+primaryPiece.getX()+", Y=0 atau "+(boardRows-1)+".");
                }
            }
            return new Board(boardRows, boardCols, identifiedPieces, exitX, exitY);

        } catch (IOException e) {
            System.err.println("Error parsing file: " + filePath + " Pesan: " + e.getMessage());
            throw e;
        } catch (Exception e) { 
            System.err.println("Error parsing konten file (Exception umum): " + filePath + " Pesan: " + e.getMessage());
            e.printStackTrace(); 
            throw new IOException("Error parsing konten file (Exception umum): " + e.getMessage(), e);
        }
    }
}