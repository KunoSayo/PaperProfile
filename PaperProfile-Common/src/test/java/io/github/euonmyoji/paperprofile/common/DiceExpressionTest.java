package io.github.euonmyoji.paperprofile.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author yinyangshi
 */
class DiceExpressionTest {

    @Test
    void testDice() throws DiceException {
        DiceExpression diceExpression = new DiceExpression("(-1d1)-2d1");
        int r = diceExpression.getTotalResult();
        Assertions.assertEquals(-3, r);
        Assertions.assertEquals("-1-(1+1)", diceExpression.toString());

        diceExpression = new DiceExpression("2d1*3");
        r = diceExpression.getTotalResult();
        Assertions.assertEquals(6, r);
        Assertions.assertEquals("(1+1)*3", diceExpression.toString());

        diceExpression = new DiceExpression("5d1-3d1");
        r = diceExpression.getTotalResult();
        Assertions.assertEquals(2, r);
        Assertions.assertEquals("(1+1+1+1+1)-(1+1+1)", diceExpression.toString());

        diceExpression = new DiceExpression("1d1*-1d1");
        r = diceExpression.getTotalResult();
        Assertions.assertEquals(-1, r);
        Assertions.assertEquals("1*-1", diceExpression.toString());
    }

    @Test
    void randomShow() throws DiceException {
        DiceExpression diceExpression = new DiceExpression("1d6+2d6*3d6");
        System.out.println(diceExpression.toString());
        System.out.println("=" + diceExpression.getTotalResult());
        diceExpression.calcAgain();
        System.out.println(diceExpression.toString());
        System.out.println("=" + diceExpression.getTotalResult());
    }
}
