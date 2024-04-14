import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui(){
        // setter gui og titel op
        super("Vejr App");

        // konfigurere gui til at lukke procesen når gui'en lukkes
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // setter størrelsen af gui'en (mål er afgivet i pixels)
        setSize(450, 650);

        // loader gui til midten af skærmen
        setLocationRelativeTo(null);

        // setter layout manager til "null" for at manuelt positionere vores componenter inde i gui'en
        setLayout(null);

        // preventere gui størrelses ændring
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents(){
        // søge fældt
        JTextField searchTextField = new JTextField();

        // sætter lokation og størrelse af gui dellene
        searchTextField.setBounds(15, 15, 351, 45);

        // ændre skrift størrelse og font
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        // indsætter billed
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/Skyet.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // temperature tekst
        JLabel temperatureText = new JLabel("0 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // centrer teksten
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // Vejr deskription
        JLabel weatherConditionDesc = new JLabel("Søg efter en by");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // indsætter billed
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // Fugtigheds tekst
        JLabel humidityText = new JLabel("<html><b>Fugtighed</b> 0%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // Vindstyrke
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // Vindstyrke tekst
        JLabel windspeedText = new JLabel("<html><b>Vindstyrke</b> 0-m/s</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // søge knap
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // ændre cursor når den hover objekt så det er mere brugervenligt
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // bruger indsætter lokation
                String userInput = searchTextField.getText();

                // Valider tekst ved at fjerne whitespace for at sikre ingen tom tekst (dette er så der ikke sker fejl i indput)
                if(userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }

                // henter vejr data
                weatherData = WeatherApp.getWeatherData(userInput);

                // updater gui

                // update vejr billed
                String weatherCondition = (String) weatherData.get("weather_condition");

                // opdater vejr billedet afhængig af nuværende tilstand og kommende tilstand af vejr
                switch(weatherCondition){
                    case "Skyfrit":
                        weatherConditionImage.setIcon(loadImage("src/assets/Skyfrit.png"));
                        break;
                    case "Skyet":
                        weatherConditionImage.setIcon(loadImage("src/assets/Skyet.png"));
                        break;
                    case "Rejn":
                        weatherConditionImage.setIcon(loadImage("src/assets/Rejn.png"));
                        break;
                    case "Sner":
                        weatherConditionImage.setIcon(loadImage("src/assets/Sne.pngImage"));
                        break;
                }

                // updaterer temperatur tekst
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                // updaterer vejr tilstands teks
                weatherConditionDesc.setText(weatherCondition);

                // updater fugtigheds tekst
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Fugtighed</b> " + humidity + "%</html>");

                // update wind speed
                double windspeed = (double) weatherData.get("windspeed");
                // konvertere fra km/h to m/s ved at bruge givne formular (afrunder det også med math functionen for at få et rundt tal)
                windspeed = Math.round((windspeed - 3) / 2.77);
                windspeedText.setText("<html><b>Vindstyrke</b> " + windspeed + " m/s</html>");
            }
        });
        add(searchButton);
    }

    // danner billeder i gui
    private ImageIcon loadImage(String resourcePath){
        try{
            // aflæsser billed fil fra stig
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // returnere et billed så det kan blice loaded/rendered
            return new ImageIcon(image);
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}









