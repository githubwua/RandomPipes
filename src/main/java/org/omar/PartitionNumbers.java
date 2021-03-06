/*
Partitions numbers.txt based on whether the element is even or odd
 */
package org.omar;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import static org.apache.beam.sdk.transforms.Partition.*;

public class PartitionNumbers {

    public interface MyOptions extends PipelineOptions{
        @Validation.Required
        @Default.String("numbers.txt")
        ValueProvider<String> getInputFile();
        void setInputFile(ValueProvider<String> value);

        @Validation.Required
        @Default.String("results/Even")
        ValueProvider<String> getEvenOutput();
        void setEvenOutput(ValueProvider<String> value);

        @Validation.Required
        @Default.String("results/Oddy")
        ValueProvider<String> getOddOutput();
        void setOddOutput(ValueProvider<String> value);

    }

    public static void main(String[] args) {
        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
        runPartitioner(options);
    }

    private static void runPartitioner(MyOptions options) {
        Pipeline p = Pipeline.create(options);

        PCollectionList<Integer> oddEvenCombined =  p.apply("read the file", TextIO.read().from(options.getInputFile()).withDelimiter(new byte[] {',','\n'})).
                apply("toInt", ParDo.of(new CovertToInt())).
                apply("Partition Stage", of(2,new EvenOddSplit()));


        PCollection<Integer> evenSteven = oddEvenCombined.get(0);
        PCollection<Integer> oddBob= oddEvenCombined.get(1);


        evenSteven.apply("Convert to String", ParDo.of(new ConvertToString())).
                apply("output the file", TextIO.write().to(options.getEvenOutput()));

        oddBob.apply("Convert to String", ParDo.of(new ConvertToString())).
                apply("output the file", TextIO.write().to(options.getOddOutput()));

        p.run().waitUntilFinish();
    }

    public static class CovertToInt extends DoFn<String, Integer> {
        @ProcessElement
        public void processElement(@Element String stringNum, OutputReceiver<Integer> outNum){
           outNum.output(Integer.parseInt(stringNum));
        }
    }

    public static class ConvertToString extends  DoFn<Integer,String> {
        @ProcessElement
        public void processElement(@Element Integer inNum, OutputReceiver<String> outString){
            outString.output(inNum.toString());
        }

    }

    private static class EvenOddSplit implements PartitionFn<Integer> {
        @Override
        public int partitionFor(Integer elem, int numPartitions) {

            if (elem%2 == 0){
                return 0;
            }
            else{
                return 1;
            }
        }
    }
}

