import type { ReactNode, MouseEventHandler, HTMLAttributes } from 'react';
import './ui.css';

interface CardProps {
  children: ReactNode;
  className?: string;
  onClick?: MouseEventHandler<HTMLDivElement>;
  hoverable?: boolean;
}

interface CardSectionProps extends Omit<HTMLAttributes<HTMLDivElement>, 'className'> {
  children: ReactNode;
  className?: string;
}

export const Card = ({
  children,
  className = '',
  onClick,
  hoverable = false,
}: CardProps): JSX.Element => {
  return (
    <div className={`card ${hoverable ? 'card-hoverable' : ''} ${className}`} onClick={onClick}>
      {children}
    </div>
  );
};

export const CardHeader = ({ children, className = '', ...rest }: CardSectionProps): JSX.Element => (
  <div className={`card-header ${className}`} {...rest}>{children}</div>
);

export const CardBody = ({ children, className = '', ...rest }: CardSectionProps): JSX.Element => (
  <div className={`card-body ${className}`} {...rest}>{children}</div>
);

export const CardFooter = ({ children, className = '', ...rest }: CardSectionProps): JSX.Element => (
  <div className={`card-footer ${className}`} {...rest}>{children}</div>
);
