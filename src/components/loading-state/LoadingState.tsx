import * as React from 'react';
import Spinner from '../../assets/img/Spinner.png';

export const LoadingState: React.FunctionComponent = () => {
  return (
    <div className="loading-spinner">
      <img src={Spinner} alt="Loading..." />
    </div>
  );
};
