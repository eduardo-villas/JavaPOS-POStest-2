package postest2;


public class CoinAcceptorConstantMapper {
	// ///////////////////////////////////////////////////////////////////
	// "EndDeposit" Method Constants
	// ///////////////////////////////////////////////////////////////////

	public static final ConstantConverter CACC_DEPOSIT_COMPLETE = new ConstantConverter(11, "BACC_DEPOSIT_COMPLETE");

	// ///////////////////////////////////////////////////////////////////
	// "PauseDeposit" Method Constants
	// ///////////////////////////////////////////////////////////////////

	public static final ConstantConverter CACC_DEPOSIT_PAUSE = new ConstantConverter(11, "BACC_DEPOSIT_PAUSE");
	public static final ConstantConverter CACC_DEPOSIT_RESTART = new ConstantConverter(12, "BACC_DEPOSIT_RESTART");

	public static int getConstantNumberFromString(String constant) {

		if (constant.equals(CoinAcceptorConstantMapper.CACC_DEPOSIT_COMPLETE.getConstant())) {
			return CoinAcceptorConstantMapper.CACC_DEPOSIT_COMPLETE.getContantNumber();
		}

		if (constant.equals(CoinAcceptorConstantMapper.CACC_DEPOSIT_PAUSE.getConstant())) {
			return CoinAcceptorConstantMapper.CACC_DEPOSIT_PAUSE.getContantNumber();
		}

		if (constant.equals(CoinAcceptorConstantMapper.CACC_DEPOSIT_RESTART.getConstant())) {
			return CoinAcceptorConstantMapper.CACC_DEPOSIT_RESTART.getContantNumber();
		}

		return Integer.parseInt(constant);
	}

}