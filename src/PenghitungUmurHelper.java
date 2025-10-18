import java.time.LocalDate;
import java.time.Period;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;
import javax.swing.JTextArea;
import org.json.JSONArray;
import org.json.JSONObject;

// Class dimulai di sini
public class PenghitungUmurHelper {

    // Method Anda yang sudah ada
    public String hitungUmurDetail(LocalDate lahir, LocalDate sekarang) {
        Period period = Period.between(lahir, sekarang);
        return period.getYears() + " tahun, " + period.getMonths() + " bulan, " + period.getDays() + " hari";
    }

    // Method Anda yang sudah ada
    public LocalDate hariUlangTahunBerikutnya(LocalDate lahir, LocalDate sekarang) {
        LocalDate ulangTahunBerikutnya = lahir.withYear(sekarang.getYear());
        if (!ulangTahunBerikutnya.isAfter(sekarang)) {
            ulangTahunBerikutnya = ulangTahunBerikutnya.plusYears(1);
        }
        return ulangTahunBerikutnya;
    }

    // Method Anda yang sudah ada
    public String getDayOfWeekInIndonesian(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
                return "Senin";
            case TUESDAY:
                return "Selasa";
            case WEDNESDAY:
                return "Rabu";
            case THURSDAY:
                return "Kamis";
            case FRIDAY:
                return "Jumat";
            case SATURDAY:
                return "Sabtu";
            case SUNDAY:
                return "Minggu";
            default:
                return "";
        }
    }

    // =================================================================
    // METHOD BARU YANG DITAMBAHKAN (DAN DIPERBAIKI)
    // =================================================================
    
    /**
     * Mendapatkan peristiwa penting secara baris per baris dari API.
     * Kode ini sesuai dengan modul Langkah 6 [cite: 1136-1198].
     */
    public void getPeristiwaBarisPerBaris(LocalDate tanggal, JTextArea txtAreaPeristiwa, Supplier<Boolean> shouldStop) {
        try {
            // Periksa jika thread seharusnya dihentikan sebelum dimulai
            if (shouldStop.get()) {
                return;
            }

            // Perbaikan: String URL harus dalam satu baris
            String urlString = "https://byabbe.se/on-this-day/" +
                    tanggal.getMonthValue() + "/" + tanggal.getDayOfMonth() + "/events.json";
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // [cite: 1148]

            int responseCode = conn.getResponseCode();
            
            // Perbaikan: String Exception harus dalam satu baris
            if (responseCode != 200) {
                throw new Exception("HTTP response code: " + responseCode + ". Silakan coba lagi nanti atau cek koneksi internet.");
            }

            // Perbaikan: Inisialisasi BufferedReader dalam satu baris
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            String inputLine;
            StringBuilder content = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                // Periksa jika thread seharusnya dihentikan saat membaca
                if (shouldStop.get()) {
                    in.close();
                    conn.disconnect();
                    javax.swing.SwingUtilities.invokeLater(() ->
                            txtAreaPeristiwa.setText("Pengambilan data dibatalkan.\n"));
                    return;
                }
                content.append(inputLine);
            }
            
            in.close();
            conn.disconnect();

            JSONObject json = new JSONObject(content.toString());
            JSONArray events = json.getJSONArray("events");

            for (int i = 0; i < events.length(); i++) {
                // Periksa jika thread seharusnya dihentikan sebelum memproses data
                if (shouldStop.get()) {
                    javax.swing.SwingUtilities.invokeLater(() ->
                            txtAreaPeristiwa.setText("Pengambilan data dibatalkan.\n"));
                    return;
                }
                
                JSONObject event = events.getJSONObject(i);
                String year = event.getString("year");
                
                // Menggunakan "description" sesuai PDF [cite: 1182]
                String description = event.getString("description"); 
                
                String peristiwa = year + ": " + description;

                // Tampilkan per baris
                javax.swing.SwingUtilities.invokeLater(() ->
                        txtAreaPeristiwa.append(peristiwa + "\n"));
            }

            // Jika tidak ada event
            if (events.length() == 0) {
                // Perbaikan: String setText harus dalam satu baris
                javax.swing.SwingUtilities.invokeLater(() ->
                        txtAreaPeristiwa.setText("Tidak ada peristiwa penting yang ditemukan pada tanggal ini."));
            }
            
        } catch (Exception e) {
            // Perbaikan: String setText harus dalam satu baris
            javax.swing.SwingUtilities.invokeLater(() ->
                    txtAreaPeristiwa.setText("Gagal mendapatkan data peristiwa: " + e.getMessage()));
        }
    }
    
} // <-- Kurung kurawal penutup Class