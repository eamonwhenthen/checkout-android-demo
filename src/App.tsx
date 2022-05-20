import * as React from 'react';
import { useProxyParams } from './hooks/useProxyParams';
import { CheckoutForm } from './components/checkout-form';
import './App.css';

const App: React.FunctionComponent = () => {
  const { options } = useProxyParams();

  return (
    <>
      {!options && <div>&nbsp;</div>}
      {options && <CheckoutForm options={options} />}
    </>
  );
};

export default App;
