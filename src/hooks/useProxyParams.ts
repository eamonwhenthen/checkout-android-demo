import {
  CheckoutSdkDropInOptions,
  Country,
  Translation,
  AlternativePaymentMethod,
  Theme as ThemeType,
  Language as LanguageType,
} from '@whenthen/checkout-sdk-react';
import { DeepPartial } from '../utils/deep-partial';

export interface UseProxyParamsResult {
  options?: CheckoutSdkDropInOptions;
}

const TRUE = 'true';

export const useProxyParams = (): UseProxyParamsResult => {
  const params = new URLSearchParams(window.location.search);
  const apiKey = params.get('apiKey');
  const flowId = params.get('flowId');
  const paramAmount = params.get('amount');
  const currencyCode = params.get('currencyCode');
  const orderId = params.get('orderId');
  const intentId = params.get('intentId');
  const paramCountries = params.get('countries');
  const hideBillingAddress = params.get('hideBillingAddress') === TRUE;
  const alternativePaymentMethods = params.get('alternativePaymentMethods');
  const isSavedCardCvcRequired = params.get('isSavedCardCvcRequired') === TRUE;
  const allowSaveCard = params.get('allowSaveCard') === TRUE;
  const customerId = params.get('customerId');
  const theme = params.get('theme');
  const paramLanguage = params.get('language');

  const getLanguage = () => {
    if (!paramLanguage) return paramLanguage;

    if (paramLanguage === 'en' || paramLanguage === 'fr') {
      return paramLanguage;
    }

    try {
      return JSON.parse(paramLanguage) as Translation;
    } catch {
      return null;
    }
  };

  const options: CheckoutSdkDropInOptions | undefined =
    apiKey && flowId && paramAmount && currencyCode
      ? {
          apiKey,
          flowId,
          amount: Number(paramAmount),
          currencyCode,
          ...(orderId && { orderId }),
          ...(intentId && { intentId }),
          ...(hideBillingAddress && { hideBillingAddress }),
          ...(allowSaveCard && { allowSaveCard }),
          ...(customerId && { customerId }),
          ...(isSavedCardCvcRequired && { isSavedCardCvcRequired }),

          /** TODO -  rework complex data types */
          ...(paramCountries && { countries: JSON.parse(paramCountries) as Country[] }),
          ...(alternativePaymentMethods && {
            alternativePaymentMethods: JSON.parse(alternativePaymentMethods) as AlternativePaymentMethod[],
          }),
          ...(theme && { theme: JSON.parse(theme) as DeepPartial<ThemeType> }),
          ...(getLanguage() && { language: getLanguage() as LanguageType | Translation }),
        }
      : undefined;

  return { options };
};
