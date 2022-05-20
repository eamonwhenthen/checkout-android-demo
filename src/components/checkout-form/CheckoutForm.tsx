import React, { FunctionComponent, useCallback, useState } from 'react';
import {
  WhenThenCheckoutDropIn,
  ErrorEventDetails,
  CheckoutSdkDropInOptions,
  AuthorizePaymentResult,
} from '@whenthen/checkout-sdk-react';
import { shape, Requireable } from 'prop-types';
import { LoadingState } from '../loading-state';

export interface CheckoutFormProps {
  options?: CheckoutSdkDropInOptions;
}

export interface CheckoutBridge {
  handleEvent: (event: string, details: AuthorizePaymentResult | ErrorEventDetails) => void;
}

let checkoutBridge: CheckoutBridge;

export const CheckoutForm: FunctionComponent<CheckoutFormProps> = ({ options }) => {
  const [isLoaded, setIsLoaded] = useState<boolean>(false);

  const handleError = useCallback((details: ErrorEventDetails) => {
    try {
      checkoutBridge?.handleEvent('error', details);
    } catch {
      // eslint-disable-next-line no-console
      console.log('error while calling handleEvent');
    }
  }, []);

  const handleLoaded = useCallback(() => {
    setIsLoaded(true);
  }, []);

  const handlePaymentComplete = useCallback((authorizePaymentResult: AuthorizePaymentResult) => {
    try {
      checkoutBridge?.handleEvent('paymentComplete', authorizePaymentResult);
    } catch {
      // eslint-disable-next-line no-console
      console.log('error while calling handleEvent');
    }
  }, []);

  return (
    <>
      {!isLoaded && <LoadingState />}
      {options && (
        <WhenThenCheckoutDropIn
          options={options}
          onError={handleError}
          onLoaded={handleLoaded}
          onPaymentComplete={handlePaymentComplete}
        />
      )}
    </>
  );
};

CheckoutForm.displayName = 'CheckoutForm';
CheckoutForm.propTypes = {
  options: shape({}) as Requireable<CheckoutSdkDropInOptions>,
};
CheckoutForm.defaultProps = {
  options: undefined,
};
