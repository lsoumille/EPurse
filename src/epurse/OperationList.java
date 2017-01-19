/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epurse;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 *
 * @author MK_Utilisateur
 */
public class OperationList {
    /*
     *  This list is in fact a cycling array. An index (nextOp) indicates
     *  the next slot which can be used for storing a new operation,
     *  the added operation erase the oldest one, numberOp is the number
     *  of operations stored
     */
    public final static short EPURSE_MAXOP = (short)5;
    private Operation [] list = null;
    private short nextOp = (short)0;
    private short numberOp = (short)0;

    public OperationList(){
        /*  TODO
         *  Create an array of EPURSE_MAXOP Operations
         */
        list = new Operation[EPURSE_MAXOP];
        for(short i = 0 ; i < EPURSE_MAXOP ; ++i){
            list[i] = new Operation();
        }
    }

    public short getNumberOp(){
        return numberOp;
    }

    public void addOperation(byte [] buffer, byte type, short offsetAmount){
        /*  TODO
         *  add a new operation
         */
        this.numberOp += 1;
        list[nextOp].setType(type);
        list[nextOp].setAmount(Util.getShort(buffer, offsetAmount));
        ++nextOp;
        if (nextOp == 5) {
            nextOp = 0;
        }
    }

    public void getOperation(byte [] buffer, short offsetResult, short index){
        /*  TODO
         *  this method extracts the indexth operation and copy it into the
         *  buffer at offsetResult. The value of index id 0 for the more
         *  recent operation and EPURSE_MAXOP-1 for the oldest operation
         */
        if(index < 0 || index > (EPURSE_MAXOP - 1)) {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
        //0 return the more recent
        //4 the oldest
        short computeIndex = (short) ((nextOp - 1) - index);
        if (computeIndex < 0)   
            computeIndex += 5;
        list[computeIndex].getOperation(buffer, offsetResult);
    }
}
