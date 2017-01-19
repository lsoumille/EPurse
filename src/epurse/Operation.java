/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epurse;

import javacard.framework.Util;

/**
 *
 * @author MK_Utilisateur
 */
public class Operation {
    private short amount;
    private byte  type;

    public Operation(){
        amount =(short)0;
        type = (byte)0;
    }

    public void setAmount(short amount){
        this.amount = amount;
    }

    public short getAmount(){
        return amount;
    }

    public void setType(byte type){
        this.type = type;
    }

    public void setOperation(byte [] buffer, byte type, short offsetAmount){
        /*  TODO
         *  this method is called when a new operation has been created;
         *  in this case, the type is the value of the INS byte and
         *  the amount is located at 'offsetAmount' in the buffer
         */
        this.amount = Util.getShort(buffer, offsetAmount);
        this.type = type;
    }

    public void getOperation(byte [] buffer, short offsetResult){
        /*  TODO
         *  this method is called when one wants to get the
         *  content of this operation to be transmitted to
         *  the CAD; to do so, the type followed by the amount
         *  must be put at 'offsetResult' in the buffer
         */
        buffer[offsetResult] = this.type;
        Util.setShort(buffer, (short) (offsetResult + 1), this.amount);
    }

}
