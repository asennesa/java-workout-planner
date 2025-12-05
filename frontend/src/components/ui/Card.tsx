import type { ReactNode, MouseEventHandler } from 'react';
import './ui.css';

interface CardProps {
  children: ReactNode;
  className?: string;
  onClick?: MouseEventHandler<HTMLDivElement>;
  hoverable?: boolean;
}

interface CardSectionProps {
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

export const CardHeader = ({ children, className = '' }: CardSectionProps): JSX.Element => (
  <div className={`card-header ${className}`}>{children}</div>
);

export const CardBody = ({ children, className = '' }: CardSectionProps): JSX.Element => (
  <div className={`card-body ${className}`}>{children}</div>
);

export const CardFooter = ({ children, className = '' }: CardSectionProps): JSX.Element => (
  <div className={`card-footer ${className}`}>{children}</div>
);
