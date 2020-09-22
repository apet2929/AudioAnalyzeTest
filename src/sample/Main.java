package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import static kotlin.io.ByteStreamsKt.readBytes;

public class Main extends Application{
    public boolean start = false;
    public double scale = 0.1;
    public int[] test;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Group root = new Group();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 600, 400));
//        System.out.println(Arrays.toString(exportAudio()));


        Slider slider = new Slider(0.1,20,1);
        Button openButton = new Button("Select Audio File");
        openButton.setLayoutX(400);
        root.getChildren().add(slider);
        root.getChildren().add(openButton);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                scale = new_val.doubleValue();
                if(test != null) {
                    for (int i = 0; i < test.length; i++) {

                        root.getChildren().set(i + 2, new Line(i / scale, 400 / 2 + test[i], i / scale, 400 / 2));
                    }

                }
                System.out.println(scale);
            }
        });



        FileChooser fileChooser = new FileChooser();
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    test = exportAudio(fileChooser.showOpenDialog(primaryStage));
                    if(test.length != 0) {
                        for (int i = 0; i < test.length; i++) {
                            root.getChildren().add(new Line(i / scale, 400 / 2 + test[i], i / scale, 400 / 2));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed");
                }
            }
        });

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public int[] exportAudio(File file) throws IOException {
        AudioInputStream in = null;
        try{
            in = AudioSystem.getAudioInputStream(file);
        }
        catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            return new int[0];
        }
        AudioFormat format = in.getFormat();
        byte[] audioBytes = readBytes(in);

        int[] result = null;
        if(format.getSampleSizeInBits() == 16){
            int samplesLength = audioBytes.length/2;
            result = new int[samplesLength];
            if(format.isBigEndian()) {
                for(int i = 0; i < samplesLength; i++){
                    byte MSB = audioBytes[i * 2];
                    byte LSB = audioBytes[i * 2 + 1];
                    result[i] = MSB << 8 | (255 & LSB);
                }
            } else {
                for(int i = 0; i < samplesLength; i+=2){
                    byte LSB = audioBytes[i*2];
                    byte MSB = audioBytes[i*2+1];
                    result[i/2] = MSB << 8 | (255 & LSB);
                }
            }
        } else{
            int samplesLength = audioBytes.length;
            result = new int[samplesLength];
            if(format.getEncoding().toString().startsWith("PCM_SIGN")){
                for(int i = 0; i < samplesLength; i++){
                    result[i] = audioBytes[i];
                }
            } else{
                for(int i = 0; i < samplesLength; ++i){
                    result[i] = audioBytes[i]-128;
                }
            }
        }
        return result;
    }
    private byte[] readBytes(AudioInputStream inputStream){
        byte[] result = new byte[0];
        byte[] buffer = new byte[32768]; //32768 = buffer size

        try{
            int bytesRead = 0;
            do {
                bytesRead = inputStream.read(buffer);
                result = buffer.clone();
            } while(bytesRead != -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
