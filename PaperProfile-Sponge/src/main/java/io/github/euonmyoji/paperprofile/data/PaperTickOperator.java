package io.github.euonmyoji.paperprofile.data;

import io.github.euonmyoji.paperprofile.common.DiceException;
import io.github.euonmyoji.paperprofile.common.DiceExpression;
import io.github.euonmyoji.paperprofile.common.Operator;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

/**
 * @author yinyangshi
 */
public class PaperTickOperator {
    public final Operator operator;
    public final DiceExpression diceExpression;
    public final String name;

    public final boolean isBuff;

    public PaperTickOperator(String v) throws ObjectMappingException {
        //不写什么s er了 累死了
        String[] args = v.split(" ", 3);
        if (args.length != 4) {
            throw new ObjectMappingException("cannot parse " + v + " to gt_op");
        }
        if (args[0].equals("attribute")) {
            isBuff = false;
        } else if (args[0].startsWith("buff_")) {
            isBuff = true;
        } else {
            throw new ObjectMappingException("cannot parse " + v + " to gt_op");
        }

        name = args[1];
        switch (args[2]) {
            case "+": {
                operator = new Operator(Integer::sum, '+');
                break;
            }
            case "-": {
                operator = new Operator((a, b) -> a - b, '-');
                break;
            }
            case "=": {
                operator = new Operator((a, b) -> b, '=');
                break;
            }
            default: {
                throw new ObjectMappingException("cannot parse " + v + " to gt_op for doesn't know operator: " + args[1]);
            }
        }

        try {
            diceExpression = new DiceExpression(args[3]);
        } catch (DiceException e) {
            throw new ObjectMappingException("cannot parse " + v + " to gt_op", e);
        }


    }
}
