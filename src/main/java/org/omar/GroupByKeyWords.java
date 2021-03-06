package org.omar;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.commons.collections4.IterableUtils;

import java.util.List;

public class GroupByKeyWords {

    public interface MyOptions extends PipelineOptions {
        @Description("Path of the file to read from")
        @Default.String("wordkeys.txt")
        ValueProvider<String> getInputFile();
        void setInputFile(ValueProvider<String> value);

        @Description("Path of the file to write to")
        @Default.String("results/GBK")
        ValueProvider<String> getOutput();
        void setOutput(ValueProvider<String> value);
    }

    public static void main(String[] args) {
        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
        runKeyWords(options);
    }

    private static void runKeyWords(MyOptions options) {
        Pipeline p = Pipeline.create(options);

        p.apply(TextIO.read().from(options.getInputFile())).
               apply(ParDo.of(new makeKV())).
               apply(GroupByKey.create()).
               apply(ParDo.of(new FormatAsText())).
                apply(TextIO.write().to(options.getOutput()));
                p.run().waitUntilFinish();
    }

    private static class makeKV extends DoFn<String, KV<String,Integer>> {
        @ProcessElement
        public void processElement(ProcessContext c){
            String[] e = c.element().split(",");
            String name = e[0];
            Integer number = Integer.parseInt(e[1]);
            c.output(KV.of(name,number));
        }
    }

    public static class FormatAsText extends DoFn<KV<String, Iterable<Integer>>, String> {
        @ProcessElement
          public void processElement(ProcessContext input) {
            String key = input.element().getKey();
            List<Integer> numList = IterableUtils.toList( input.element().getValue());
            input.output(key + ": " + numList);
        }
    }

}
