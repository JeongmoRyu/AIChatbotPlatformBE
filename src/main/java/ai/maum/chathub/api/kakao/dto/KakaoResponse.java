package ai.maum.chathub.api.kakao.dto;

import ai.maum.chathub.util.ObjectMapperUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KakaoResponse {
    private String version = "2.0";
    @JsonProperty("useCallback")
    private Boolean useCallback;
//    private Context context;
    private Data data;
    private Template template;

    public void setOutputSimpleText(String text) {

        if (this.template == null) {
            this.template = new Template();
        }

        Template.Outputs outputs = new Template.Outputs();
        Template.Outputs.SimpleText simpleText = new Template.Outputs.SimpleText(text);
        outputs.setSimpleText(simpleText);

        List<Template.Outputs> outputlist = new ArrayList<Template.Outputs>();
        outputlist.add(outputs);

        this.template.setOutputs(outputlist);
    }

    public void setOutputSimpleImage(String imageUrl, String altText) {
        if (this.template == null) {
            this.template = new Template();
        }

        Template.Outputs outputs = new Template.Outputs();
        Template.Outputs.SimpleImage simpleImage = new Template.Outputs.SimpleImage(imageUrl, altText);
        outputs.setSimpleImage(simpleImage);

        List<Template.Outputs> outputlist = new ArrayList<Template.Outputs>();
        outputlist.add(outputs);

        this.template.setOutputs(outputlist);
    }

    public void setDataText(String text) {

        if (this.data == null) {
            this.data = new Data();
        }
        this.data.setText(text);
    }

    public KakaoResponse() {
    }

    public KakaoResponse(String version, Boolean useCallback, Data data) {
        this.version = version;
        this.useCallback = useCallback;
        this.data = data;
    }

    @Getter @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Template {

        public Template() {
        }

        public Template(List<Outputs> outputs) {
            this.outputs = outputs;
        }

        private List<Outputs> outputs;

        @Getter @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Outputs {

            public Outputs() {
            }

            public Outputs(SimpleText simpleText, TextCard textCard, SimpleImage simpleImage) {
                this.simpleText = simpleText;
                this.textCard = textCard;
                this.simpleImage = simpleImage;
            }

            @JsonProperty("simpleText")
            private SimpleText simpleText;
            @JsonProperty("textCard")
            private TextCard textCard;
            @JsonProperty("simpleImage")
            private SimpleImage simpleImage;
//            private BasicCard basicCard;
//            private CommerceCard commerceCard;
//            private ListCard listCard;
//            private ItemCard itemCard;



            @Getter @Setter
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class SimpleText {
                private String text;

                public SimpleText() {
                }

                public SimpleText(String text) {
                    this.text = text;
                }
            }

            @Getter @Setter
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class TextCard {
                private String title;
                private String description;
                private List<Button> buttons;

                public TextCard() {
                }

                public TextCard(String title, String description, List<Button> buttons) {
                    this.title = title;
                    this.description = description;
                    this.buttons = buttons;
                }
            }

            @Getter @Setter
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class SimpleImage {
                @JsonProperty("imageUrl")
                private String imageUrl;
                @JsonProperty("altText")
                private String altText;

                public SimpleImage() {
                }

                public SimpleImage(String imageUrl, String altText) {
                    this.imageUrl = imageUrl;
                    this.altText = altText;
                }
            }

        }
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        private String text;

        public Data() {
        }

        public Data(String text) {
            this.text = text;
        }
    }

    @Getter @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Link {
        private String pc;
        private String mobile;
        private String web;

        public Link() {
        }

        public Link(String pc, String mobile, String web) {
            this.pc = pc;
            this.mobile = mobile;
            this.web = web;
        }
    }


    @Getter @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Thumnail {
        private String imageUrl;
        private Link link;
        private Boolean fixedRatio;

        public Thumnail() {
        }

        public Thumnail(String imageUrl, Link link, boolean fixedRatio) {
            this.imageUrl = imageUrl;
            this.link = link;
            this.fixedRatio = fixedRatio;
        }
    }

    @Getter @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Button {
        private String label;
        private String action;
//        private String webLinkeUrl;
//        private String messageText;
//        private String phoneNumber;
        private String blockId;
//        private Map<String, Object> extra;


        public Button() {
        }

        public Button(String label, String action, String blockId) {
            this.label = label;
            this.action = action;
            this.blockId = blockId;
        }
    }


    public void setTextCardWithOneButton(String title, String action, String label, String blockId) {

        KakaoResponse.Button button = new KakaoResponse.Button();
        button.setAction(action);
        button.setLabel(label);
        button.setBlockId(blockId);

        KakaoResponse.Template.Outputs.TextCard textCard = new KakaoResponse.Template.Outputs.TextCard();
        List<KakaoResponse.Button> buttons = new ArrayList<>();
        buttons.add(button);
        textCard.setButtons(buttons);
        textCard.setTitle(title);

        List<KakaoResponse.Template.Outputs> outputs = new ArrayList<>();
        KakaoResponse.Template.Outputs output = new KakaoResponse.Template.Outputs();
        output.setTextCard(textCard);
        outputs.add(output);
        if (this.template == null) {
            this.template = new Template();
        }

        this.template.setOutputs(outputs);

        log.debug(ObjectMapperUtil.writeValueAsString(this));

    }
}
