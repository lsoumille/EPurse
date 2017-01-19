/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epurse;

import javacard.framework.*;

/**
 *
 * @author MK_Utilisateur
 */
public class EPurse extends Applet {

    public final static byte EPURSE_CLA = (byte)0xA0;
    public final static byte EPURSE_BAL = (byte)0xB0;
    public final static byte EPURSE_ADD = (byte)0xB2;
    public final static byte EPURSE_SUB = (byte)0xB4;

    public final static byte EPURSE_MAX_LENGTH  = (byte)2;
    public final static byte EPURSE_MAX_TRY     = (byte)3;
    public final static byte EPURSE_CHK         = (byte)0x20;
    public final static byte EPURSE_CHG         = (byte)0x22;

    /*  TODO
     *  Add two new constants to support the reading of
     *  the number of operations allready memorized and
     *  for reading the ith operation
     */
    public final static byte EPURSE_NUMBER_OP = (byte)0xB6;
    public final static byte EPURSE_ITH_OP    = (byte)0xB8;

    private short balance = (short)0;
    private OwnerPIN pin = null;

    /*  TODO
     *  Add an OperationList reference field
     */
    private OperationList opList = null;
    
    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new EPurse();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected EPurse() {
        /*  TODO
         *  Create the OperationList object
         */
        opList = new OperationList();
        pin = new OwnerPIN(EPURSE_MAX_TRY, EPURSE_MAX_LENGTH );
        pin.update(new byte[] { (byte) 0x00, (byte) 0x00 }, (short) 0,  EPURSE_MAX_LENGTH);
        register();
    }

    public boolean select(){
        pin.reset();
        return true;
    }
    
    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     */
    public void process(APDU apdu) {
        //Insert your code here
        byte [] buffer = apdu.getBuffer();

        if(this.selectingApplet())
            return;

        if(buffer[ISO7816.OFFSET_CLA] != EPURSE_CLA)
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        short amount = (short)0;
        short index = (short)0;

        switch(buffer[ISO7816.OFFSET_INS]){
            case EPURSE_BAL:
                Util.setShort(buffer, (short)0, balance);
                apdu.setOutgoingAndSend((short)0, (short)2);
                break;
            case EPURSE_ADD:
                apdu.setIncomingAndReceive();
                if(!pin.isValidated())
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                amount = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
                if(amount <=0 || (short)(balance+amount) <= 0) // overloading
                    ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                else {
                    try {
                        JCSystem.beginTransaction();
                        balance += amount;
                        /*  TODO
                        *  Add a new operation
                        */                  
                        opList.addOperation(buffer, EPURSE_ADD, ISO7816.OFFSET_CDATA);
                        JCSystem.commitTransaction();
                    } catch (TransactionException e) {
                        JCSystem.abortTransaction();
                        ISOException.throwIt(e.getReason());
                    }
                    
                }  
                break;
            case EPURSE_SUB:
                apdu.setIncomingAndReceive();
                if(!pin.isValidated())
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                amount = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
                if(amount <= 0 || balance < amount) // overloading
                    ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                else {
                    try {
                        JCSystem.beginTransaction();
                        balance -= amount;
                        /*  TODO
                        *  Add a new operation
                        */
                        opList.addOperation(buffer, EPURSE_SUB, ISO7816.OFFSET_CDATA);
                        JCSystem.commitTransaction();
                    } catch(TransactionException e) {
                        JCSystem.abortTransaction();
                        ISOException.throwIt(e.getReason());
                    }
                    
                }
                break;
            case EPURSE_CHK:
                apdu.setIncomingAndReceive();
                if(!pin.check(buffer, ISO7816.OFFSET_CDATA, EPURSE_MAX_LENGTH))
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                break;
            case EPURSE_CHG:
                apdu.setIncomingAndReceive();
                if(!pin.isValidated())
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                pin.update(buffer,ISO7816.OFFSET_CDATA, EPURSE_MAX_LENGTH);
                break;
            /*  TODO
             *  add the process of the two new commands
             */
            case EPURSE_NUMBER_OP:           
                if(!pin.isValidated())
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);                
                Util.setShort(buffer, (short)0, opList.getNumberOp());
                apdu.setOutgoingAndSend((short)0, (short)2);
                break;
            case EPURSE_ITH_OP:
                apdu.setIncomingAndReceive();
                if(!pin.isValidated())
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);                
                index = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
                opList.getOperation(buffer, (short) 0, index);
                apdu.setOutgoingAndSend((short)0, (short)3);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
