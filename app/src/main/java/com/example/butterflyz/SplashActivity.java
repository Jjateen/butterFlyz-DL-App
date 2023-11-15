package com.example.butterflyz;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.butterflyz.ml.ButterX;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SplashActivity extends AppCompatActivity {

    TextView result;
    ImageView imageView;
    Button picture;
    Button sel;
    Button predictButton;
    int imageSize = 224;
    private Bitmap img;
    private View scanningOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);
        scanningOverlayView = findViewById(R.id.scanningOverlayView);
        picture = findViewById(R.id.button);
        sel = findViewById(R.id.button2);
        predictButton = findViewById(R.id.predictButton);

        sel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    Intent filePickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(filePickerIntent, 2);
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 200);
                }
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                               Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                               startActivityForResult(cameraIntent, 1);
                                           } else {
                                               requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                                           }
                                       }
                                   });
        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the scanning overlay to start the animation
                scanningOverlayView.setVisibility(View.VISIBLE);
                scanningOverlayView.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.scanning_animation));

                // Add a delay before stopping the animation and calling classifyImage()
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Hide the scanning overlay to stop the animation
                        scanningOverlayView.clearAnimation();
                        scanningOverlayView.setVisibility(View.GONE);

                        // Call classifyImage() after the animation has stopped
                        classifyImage(img);
                    }
                }, 2050); // 2050 milliseconds (2.05 seconds) delay
            }
        });
    }
    public void classifyImage(Bitmap image) {
        try {
            ButterX model = ButterX.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            ButterX.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {
                    "ADONIS","AFRICAN GIANT SWALLOWTAIL","AMERICAN SNOOT","AN 88","APPOLLO","ARCIGERA FLOWER MOTH","ATALA","ATLAS MOTH","BANDED ORANGE HELICONIAN","BANDED PEACOCK","BANDED TIGER MOTH","BECKERS WHITE","BIRD CHERRY ERMINE MOTH","BLACK HAIRSTREAK","BLUE MORPHO","BLUE SPOTTED CROW","BROOKES BIRDWING","BROWN ARGUS","BROWN SIPROETA","CABBAGE WHITE","CAIRNS BIRDWING","CHALK HILL BLUE","CHECQUERED SKIPPER","CHESTNUT","CINNABAR MOTH","CLEARWING MOTH","CLEOPATRA","CLODIUS PARNASSIAN","CLOUDED SULPHUR","COMET MOTH","COMMON BANDED AWL","COMMON WOOD-NYMPH","COPPER TAIL","CRECENT","CRIMSON PATCH","DANAID EGGFLY","EASTERN COMA","EASTERN DAPPLE WHITE","EASTERN PINE ELFIN","ELBOWED PIERROT","EMPEROR GUM MOTH","GARDEN TIGER MOTH","GIANT LEOPARD MOTH","GLITTERING SAPPHIRE","GOLD BANDED","GREAT EGGFLY","GREAT JAY","GREEN CELLED CATTLEHEART","GREEN HAIRSTREAK","GREY HAIRSTREAK","HERCULES MOTH","HUMMING BIRD HAWK MOTH","INDRA SWALLOW","IO MOTH","Iphiclus sister","JULIA","LARGE MARBLE","LUNA MOTH","MADAGASCAN SUNSET MOTH","MALACHITE","MANGROVE SKIPPER","MESTRA","METALMARK","MILBERTS TORTOISESHELL","MONARCH","MOURNING CLOAK","OLEANDER HAWK MOTH","ORANGE OAKLEAF","ORANGE TIP","ORCHARD SWALLOW","PAINTED LADY","PAPER KITE","PEACOCK","PINE WHITE","PIPEVINE SWALLOW","POLYPHEMUS MOTH","POPINJAY","PURPLE HAIRSTREAK","PURPLISH COPPER","QUESTION MARK","RED ADMIRAL","RED CRACKER","RED POSTMAN","RED SPOTTED PURPLE","ROSY MAPLE MOTH","SCARCE SWALLOW","SILVER SPOT SKIPPER","SIXSPOT BURNET MOTH","SLEEPY ORANGE","SOOTYWING","SOUTHERN DOGFACE","STRAITED QUEEN","TROPICAL LEAFWING","TWO BARRED FLASHER","ULYSES","VICEROY","WHITE LINED SPHINX MOTH","WOOD SATYR","YELLOW SWALLOW TAIL","ZEBRA LONG WING"
            };

            String[] lifeSpan = {
                    "A few weeks to a couple of months", "Several weeks as an adult", "A few weeks to a couple of months", "Short lifespan, varies", "About a month as an adult", "Lifespan varies", "About 1 to 2 weeks as an adult", "A few days to a couple of weeks", "A few months as an adult", "Several weeks as an adult", "Lifespan varies", "Typically, a few weeks as an adult", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "A few weeks to a couple of months", "Several weeks as an adult", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Several weeks as an adult", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "A few weeks to a couple of months", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies", "Lifespan varies"
            };
            String[] rarity = {
                    "Common", "Common", "Common", "Common", "Common", "Common", "Rare", "Rare", "Common", "Common", "Common", "Common", "Common", "Rare", "Common", "Common", "Rare", "Common", "Common", "Common", "Rare", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Rare", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Rare", "Common", "Rare", "Common", "Common", "Common",
                    "Common", "Common", "Common", "Common", "Rare", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Rare", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Rare", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common",
                    "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Rare", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common", "Common"
            };

            String[] funFact = {
                    "Adonis butterflies are known for their striking red-orange coloration and are often considered a symbol of beauty in the butterfly world.", "This butterfly is one of the largest in Africa, with a wingspan that can reach up to 160 millimeters.", "The American Snoot butterfly is a fast and agile flier, known for its acrobatic flight patterns.", "The An 88 butterfly gets its name from the number '88' often found on its wings, resembling the number 88.", "The Appollo butterfly is often associated with mythology and named after the Greek god Apollo.", "This moth is part of the Tiger Moth family and is known for its distinctive and colorful appearance.", "The Atala butterfly has a unique relationship with a specific plant, the Coontie, which serves as its host plant for caterpillar development.", "The Atlas Moth is one of the largest moths in the world and has transparent 'windows' on its wings to deter predators.", "This butterfly is known for its bright orange coloration, which serves as a warning to predators that it is toxic.", "The Banded Peacock has distinctive blue and black markings on its wings, resembling the eyespots on a peacock's tail.", "Some tiger moths produce ultrasonic sounds that deter predators, including bats, which hunt them.", "The Beckers White butterfly is known for its rapid and erratic flight pattern.", "This moth is named for its association with the bird cherry tree, where its caterpillars feed.", "The Black Hairstreak butterfly is known for its striking 'hairstreak' pattern on the hindwing.", "The Blue Morpho butterfly's vivid blue coloration is due to microscopic scales that reflect light.", "This butterfly is a proficient mimic, resembling other toxic butterflies to deter predators.", "The Brookes Birdwing is one of the largest butterflies in Australia and is known for its impressive size and coloration.", "The Brown Argus butterfly is often found in meadows and grassy areas.", "This butterfly is known for its unique wing shape and coloration, resembling a leaf.", "Cabbage White butterflies are known for their habit of laying eggs on plants in the cabbage family.", "The Cairns Birdwing butterfly is one of the largest butterflies in Australia and is known for its vibrant green coloration and distinctive yellow and black markings.", "Chalk Hill Blue butterflies are known for their striking sky-blue coloration, and they are typically found in chalky and limestone grasslands.", "Chequered Skipper butterflies have a fast and erratic flight pattern, making them a challenge to capture in flight.", "The Chestnut butterfly gets its name from the chestnut-brown color of its wings, and it's often found in woodland areas.", "Cinnabar Moths are known for their striking black and red coloration and are associated with toxic ragwort plants, which their caterpillars feed on.", "Clearwing Moths are named for their transparent wings and are often mistaken for wasps due to their appearance.", "The Cleopatra butterfly is named after the famous Egyptian queen and is known for its vibrant orange coloration.", "Clodius Parnassian butterflies are typically found in mountainous regions and are known for their white wings with black markings.", "Clouded Sulphur butterflies are highly variable in color, ranging from pale yellow to greenish-gray.", "Comet Moths are known for their long, trailing tails, which resemble the tail of a comet.", "The Common Banded Awl butterfly has distinctive markings on its wings and is often found in forested areas.", "This butterfly is known for its delicate appearance and is often found in wooded and grassy areas.", "Copper Tail butterflies have vibrant orange or copper-colored markings on their hindwings.", "Crescent butterflies have crescent-shaped markings on their wings, and they are often found in open, grassy areas.", "Crimson Patch butterflies are known for their striking red coloration and are often found in Central and South America.", "Danaid Eggfly butterflies are named for their distinctive egg-shaped markings on their wings.", "Eastern Coma butterflies are known for their beautiful blue coloration and are often found in South America.", "Eastern Dapple White butterflies are named for their mottled appearance and are often found in woodlands.", "Eastern Pine Elfin butterflies are typically small and are often found in pine forests.", "Elbowed Pierrot butterflies are known for their small size and intricate black and white markings.", "The Emperor Gum Moth is one of the largest moths in Australia and is known for its impressive size and appearance.", "Garden Tiger Moths are known for their striking black and orange coloration and are often found in garden areas.", "The Giant Leopard Moth is named for its distinctive leopard-like spots on its wings.", "Glittering Sapphire butterflies are known for their brilliant, iridescent blue coloration.", "Gold Banded butterflies are characterized by their vibrant yellow and black markings on their wings.", "The Great Eggfly butterfly is named for its unique, egg-shaped patterns on its wings.", "Great Jay butterflies are known for their striking blue coloration and are often found in forested areas.", "This butterfly is named for the green cell-like markings on its wings.", "Green Hairstreak butterflies are known for their bright green coloration and are often found in woodland and grassy areas.", "Grey Hairstreak butterflies have distinctive gray wings with a delicate and intricate pattern.",
            "The Hercules Moth is one of the largest and heaviest moths in the world, with impressive wingspan and size.", "This moth is known for its remarkable ability to hover and feed on nectar, much like a hummingbird.", "Indra Swallow butterflies are known for their striking blue coloration and are often found in South Asia.", "The Io Moth is named for the large eyespots on its wings, which resemble the eyes of the mythical figure Io.", "Iphiclus sister butterflies are known for their intricate wing patterns and are often found in South and Central America.", "Julia butterflies are known for their vibrant orange coloration and are often found in the Americas.", "Large Marble butterflies are known for their large size and are often found in forested areas.", "Luna Moths are known for their stunning pale green coloration and distinctive long tails.", "The Madagascan Sunset Moth is known for its vibrant, sunset-like coloration and is native to Madagascar.", "Malachite butterflies are known for their striking green coloration and are often found in tropical regions.", "Mangrove Skipper butterflies are often found in coastal areas and are known for their adaptability to different habitats.", "Mestra butterflies are named after a mythological figure and are known for their subtle brown and orange coloration.", "Metalmark butterflies are characterized by metallic spots on their wings, which give them a distinctive appearance.", "Milbert's Tortoiseshell butterflies have a distinctive orange and black pattern on their wings and are often found in North America.", "Monarch butterflies are famous for their long-distance migrations and their unique ability to navigate to specific wintering sites.", "Mourning Cloak butterflies are known for their deep, velvety appearance and are often found in temperate regions.", "The Oleander Hawk Moth is a large and powerful flier, often mistaken for a hummingbird due to its hovering flight.", "The Orange Oakleaf butterfly resembles a dead leaf when its wings are closed, serving as camouflage.", "Orange Tip butterflies are named for the distinctive orange tips on their wings and are often found in open areas.", "Orchard Swallow butterflies are known for their swift and agile flight patterns.", "Painted Lady butterflies are known for their impressive migratory journeys and their ability to cover long distances.", "Paper Kite butterflies have delicate and translucent wings, resembling paper.", "Peacock butterflies are named for the distinctive 'peacock eye' markings on their wings.", "Pine White butterflies are often found in forested areas and are known for their white coloration.", "Pipevine Swallow butterflies are associated with pipevine plants, which are toxic and deter predators.", "The Polyphemus Moth is named after the one-eyed giant in Greek mythology and has eye-like spots on its wings.", "Popinjay butterflies are known for their colorful and eye-catching appearance.", "Purple Hairstreak butterflies are known for their stunning purple coloration and subtle hairstreak patterns.", "The Purplish Copper butterfly has a distinctive purplish sheen on its wings.", "Question Mark butterflies are named for the 'question mark' shape on the underside of their wings.", "Red Admiral butterflies are known for their striking red and black coloration and are found in various habitats.", "Red Cracker butterflies are named for the distinctive cracking sound they make when in flight.", "Red Postman butterflies are known for their brilliant red coloration, and they are often found in Central and South America.", "Red Spotted Purple butterflies have a unique combination of red spots on a dark background, resembling tree bark.", "Rosy Maple Moths are often called the 'cotton candy moth' due to their pink and yellow coloration.", "Scarce Swallow butterflies are known for their swift and graceful flight patterns.", "Silver Spot Skipper butterflies have distinctive silver spots on their wings.", "Sixspot Burnet Moths are known for their metallic sheen and are often found in grassy areas.", "Sleepy Orange butterflies are known for their subtle orange coloration.", "Sootywings are often found in open, sunny areas and are named for their dark, sooty appearance.", "The Southern Dogface butterfly resembles the face of a dog on its wings, giving it its name.", "Straited Queen butterflies are often found in open areas and are known for their rapid flight.", "Tropical Leafwing butterflies are often found in tropical rainforests and are known for their leaf-like appearance.", "Two Barred Flasher butterflies are named for the two distinctive bars on their wings.", "Ulysses butterflies are known for their striking electric blue coloration and are often found in Australia.", "Viceroy butterflies are known for their mimicry of the toxic Monarch butterfly and are often found in North America.", "White Lined Sphinx Moths are large and powerful fliers, often mistaken for hummingbirds.", "Wood Satyr butterflies are often found in wooded areas and are known for their subtle coloration.", "Yellow Swallowtail butterflies are known for their bright yellow coloration and distinctive 'swallowtail' tails.", "Zebra Long Wing butterflies are named for their long, striped wings."
            };

            String res = classes[maxPos]+ " : " + String.format("%.2f", confidences[maxPos] * 100) + "%\n" + lifeSpan[maxPos] + "\n" + rarity[maxPos] + "\n\n" + funFact[maxPos];
            result.setText(res);

            model.close();
        } catch (IOException e) {
            // Handle the exception
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int w = image.getWidth();
            int h = image.getHeight();
            image = ThumbnailUtils.extractThumbnail(image, w, h);
            imageView.setImageBitmap(image);
            img = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);
            result.setText("");
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                int w = image.getWidth();
                int h = image.getHeight();
                image = ThumbnailUtils.extractThumbnail(image, w, h);
                imageView.setImageBitmap(image);
                img = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);
                result.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
